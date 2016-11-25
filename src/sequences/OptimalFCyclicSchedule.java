package sequences;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;
import optimization.Monomial;
import optimization.OptimizationConstraint;
import optimization.OptimizationProblem;
import optimization.OptimizationVar;
import optimization.Posynomial;
import optimization.SingleIndexOptimizationVar;
import system.Item;

/**
 * Implements the optimization problem (4.22) in thesis to find the optimal
 * schedule for a given production sequence.
 * 
 * @author ftubilla
 *
 */
@CommonsLog
public class OptimalFCyclicSchedule {

    private final double machEfficiency;
    private final OptimizationProblem problem;
    private final SingleIndexOptimizationVar<Integer> sprintingWithInventoryTime;
    private final SingleIndexOptimizationVar<Integer> sprintingWithBacklogTime;
    private final SingleIndexOptimizationVar<Integer> cruisingTime;
    private final OptimizationVar cycleTime;
    private final ProductionSequence sequence;
    private final List<Integer> sequencePositions;

    public OptimalFCyclicSchedule(final ProductionSequence sequence, final double machineEfficiency) {
        this.machEfficiency = machineEfficiency;
        this.sequence = sequence;
        this.sequencePositions = new ArrayList<>();
        int n = 0;
        for (@SuppressWarnings("unused") Item item : sequence ) {
            this.sequencePositions.add(n++);
        }
        this.problem = new OptimizationProblem("Optimal f-cyclic schedule for " + sequence);
        this.sprintingWithInventoryTime = new SingleIndexOptimizationVar<>("DTnsi", this.sequencePositions);
        this.sprintingWithBacklogTime = new SingleIndexOptimizationVar<>("DTnsb", this.sequencePositions);
        this.cruisingTime = new SingleIndexOptimizationVar<>("DTnc", this.sequencePositions);
        this.cycleTime = new OptimizationVar("Tf");
        this.problem.addVar(this.sprintingWithInventoryTime);
        this.problem.addVar(this.sprintingWithBacklogTime);
        this.problem.addVar(this.cruisingTime);
        this.problem.addVar(this.cycleTime);
        
        for ( Item item : sequence ) {
            if ( !Double.isFinite( item.getBacklogCostRate() ) ||
                    !Double.isFinite( item.getInventoryCostRate() ) ) {
                throw new IllegalArgumentException("Inventory/backlog costs are not finite!");
            }
        }
    }

    public void compute() throws Exception {

        // Start with the objective function
        Posynomial objPosynomial = new Posynomial();

        for ( int n : this.sequencePositions ) {
            //Get the items parameters and compensate for the machine efficiency 
            Item item = this.sequence.getItemAtPosition(n);
            double h = item.getInventoryCostRate();
            double b = item.getBacklogCostRate();
            double mu = item.getProductionRate() * this.machEfficiency;
            double d = item.getDemandRate();
            double rho = d / mu;
            int nextN = this.sequence.getNextPosition(n); // Next position where the item occurs

            // h (mu-d)/ 2rho (DTnsi)^2
            Monomial m1 = new Monomial();
            m1.mult(this.sprintingWithInventoryTime.get(n), 2);
            m1.mult(h * (mu - d) / ( 2 * rho ) );
            m1.mult( this.cycleTime, -1);

            // b (mu-d) / 2 (DTnsb)^2
            Monomial m2 = new Monomial();
            m2.mult(this.sprintingWithBacklogTime.get(n), 2);
            m2.mult(b * (mu-d) / 2.0);
            m2.mult( this.cycleTime, -1);
            
            // b (mu-d)^2/ 2d (DTsbv(n))^2
            Monomial m3 = new Monomial();
            m3.mult(this.sprintingWithBacklogTime.get(nextN), 2);
            m3.mult(b * Math.pow( mu - d, 2) / ( 2 * d ));
            m3.mult( this.cycleTime, -1);
            
            // Add the terms and divide by Tf
            objPosynomial.add(m1).add(m2).add(m3);

            // Add the constraint ensuring consistency between production times and inventory/backlog levels
            // ( DTsni - DTsv(n)b ) ( mu - d ) - sum_{m inbetween} d ( DTsmi + DTsmB + DTmc ) = setupTime * d 
            OptimizationConstraint ctr = new OptimizationConstraint("eq4_21_position_" + n);
            ctr.addTerm(this.sprintingWithInventoryTime.get(n), mu - d);
            double demandDuringSetups = item.getSetupTime() * d;
            for ( int m : this.sequence.getPositionsInBetween(n) ) {
                ctr.addTerm(this.sprintingWithInventoryTime.get(m), -d);
                ctr.addTerm(this.sprintingWithBacklogTime.get(m), -d);
                ctr.addTerm(this.cruisingTime.get(m), -d);
                // Accumulate the demand during setups
                Item mItem = this.sequence.getItemAtPosition(m);
                demandDuringSetups += mItem.getSetupTime() * d;
            }
            ctr.addTerm(this.sprintingWithBacklogTime.get(nextN),  (mu - d) );
            ctr.eql(demandDuringSetups);
            this.problem.addConstraint(ctr);
        }

        // Non-Negativity
        for ( int n : this.sequencePositions ) {
            // Sprinting with inventory
            OptimizationConstraint sprintingINN = new OptimizationConstraint("DTnsi>=0_" + n);
            sprintingINN.addTerm(this.sprintingWithInventoryTime.get(n), 1.0).gEql(0.0);
            this.problem.addConstraint(sprintingINN);
            // Sprinting with backlog
            OptimizationConstraint sprintingBNN = new OptimizationConstraint("DTnsb>=0_" + n);
            sprintingBNN.addTerm(this.sprintingWithBacklogTime.get(n), 1.0).gEql(0.0);
            this.problem.addConstraint(sprintingBNN);
            // Cruising
            OptimizationConstraint cruisingNN = new OptimizationConstraint("DTnc>=0_" + n);
            cruisingNN.addTerm(this.cruisingTime.get(n), 1.0).gEql(0.0);
            this.problem.addConstraint(cruisingNN);
        }
        OptimizationConstraint cycleTimeNN = new OptimizationConstraint("cycle_time>=0");
        cycleTimeNN.addTerm(this.cycleTime, 1.0).gEql(0.0);
        this.problem.addConstraint(cycleTimeNN);

        // Load the objective function
        this.problem.setObj(objPosynomial.getConvexFunction(this.problem.getVariablesMap()));

        // Add the constraint relating sum of all activities to cycle time
        // sum_n (DTnsb + DTnsi + DTnc + Sfn) = Tf or
        // sum_n (DTnsb + DTnsi + DTnc ) - Tf = -total_setup_time
        OptimizationConstraint sumCtr = new OptimizationConstraint("cycle_time");
        double totalSetupTime = 0;
        for ( int n : this.sequencePositions ) {
            sumCtr.addTerm(this.sprintingWithInventoryTime.get(n), 1.0);
            sumCtr.addTerm(this.sprintingWithBacklogTime.get(n), 1.0);
            sumCtr.addTerm(this.cruisingTime.get(n), 1.0);
            Item item = this.sequence.getItemAtPosition(n);
            totalSetupTime += item.getSetupTime();
        }
        sumCtr.addTerm(this.cycleTime, -1.0);
        sumCtr.eql(-totalSetupTime);
        this.problem.addConstraint(sumCtr);
        
        
        // Solve and store the solution
        log.debug(String.format("Optimal f-cycle schedule, Sequence:%s%nObjective:%s%nConstraints:%s%n",
                this.sequence, objPosynomial, this.problem));
        // TODO Move this to a config (also on the lower bound)
        this.problem.setTolerance(1e-5);
        this.problem.setToleranceFeas(1e-5);
        this.problem.solve();
        for ( int n : this.sequencePositions ) {
            log.debug(String.format("Sprinting time (B) = %.5f (I) = %.5f, Cruising time = %.5f",
                    this.sprintingWithBacklogTime.get(n).getSol(),
                    this.sprintingWithInventoryTime.get(n).getSol(),
                    this.cruisingTime.get(n).getSol()));
            log.debug(String.format("Cost %.5f, Cycle Time %.5f",
                    this.problem.getOptimalCost(),
                    this.cycleTime.getSol()));
        }
    }

    public Double getScheduleCost() {
        return this.problem.getOptimalCost();
    }

    public Double getScheduleCycleTime() {
        return this.cycleTime.getSol();
    }

    public ProductionSequence getSequence() {
        return this.sequence;
    }
    
    public Double getOptimalSprintingTimeWithInventory(final int position) {
        return this.sprintingWithInventoryTime.get(position).getSol();
    }
    
    public Double getOptimalSprintingTimeWithBacklog(final int position) {
        return this.sprintingWithBacklogTime.get(position).getSol();
    }
    
    public Double getOptimalCruisingTime(final int position) {
        return this.cruisingTime.get(position).getSol();
    }

    @Override
    public String toString() {
        return String.format("Optimal f-cyclic schedule for sequence %s", this.sequence);
    }
}
