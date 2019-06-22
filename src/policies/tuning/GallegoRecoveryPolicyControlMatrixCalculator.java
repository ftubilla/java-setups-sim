package policies.tuning;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import lombok.extern.apachecommons.CommonsLog;
import params.Params;
import sequences.ProductionSequence;
import sim.SimMain;

/**
 * Computes the control matrix G in Gallego's recovery policy, by solving the Discrete Algebraic Riccati
 * Equation. To run, this class requires an installation of Python and the slycot routines.
 * 
 * @author ftubilla
 *
 */
@CommonsLog
public class GallegoRecoveryPolicyControlMatrixCalculator {

    private final Params params;

    public GallegoRecoveryPolicyControlMatrixCalculator(final Params params) {
        this.params = params;
    }

    /**
     * Computes a matrix G, using the input from the params object (given at construction).
     * 
     * @param sequence
     * @param boolean indicating if the production rate should be compensated for machine failures
     * @param tolerance Max relative error allowed on entries of the M matrix solved in the Ricatti equation
     * @return matrix G
     * @throws Exception
     */
    public double[][] compute(final ProductionSequence sequence, final double tolerance) throws Exception {

        String dir = System.getProperty("user.dir");
        String pythonPath = SimMain.getProperties().getProperty("python.path");
        String scriptPath = dir + File.separator + "python" + File.separator + "grp.py";

        // Write the params as a JSON object to pass as argument
        ObjectMapper mapper = new ObjectMapper();
        String paramsJson = mapper.writeValueAsString(new ParamsForPython(params, tolerance));

        // Write the sequence
        String sequenceString = sequence.toString();
        ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath, paramsJson, sequenceString);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

        // Collect the output into the matrix (note that the output could be mixed with stderr output)
        final double[][] G = new double[sequence.getSize()][params.getNumItems()];
        final boolean[] toleranceNotMet = { false };
        final int[] rowsRead = { 0 };
        in.lines().forEach(l -> {
            try {
                String[] line = l.split(" ");
                int i = Integer.parseInt(line[0]);
                int j = Integer.parseInt(line[1]);
                double g = Double.parseDouble(line[2]);
                G[i][j] = g;
                rowsRead[0] += 1;
                log.trace(String.format("Gallego G[%d,%d]=%.5f", i, j, g));
            } catch (NumberFormatException e) {
                if ( l.contains("TOLERANCE NOT MET" ) ) {
                    log.error(String.format("Could not parse line %s", l), e);
                    toleranceNotMet[0] = true;
                } else {
                    log.trace(String.format("Could not parse line %s", l));
                }
            }
        });

        p.waitFor();

        if ( rowsRead[0] < sequence.getSize() || toleranceNotMet[0] ) {
            throw new Exception("Could not compute G");
        }

        return G;
    }
    
    /*
     * A wrapper class to expose only the set of parameters that are needed
     * for computing the G matrix in the Python script.
     */
    public static class ParamsForPython {

        private final Params params;
        private final double tol;
        private final double machineEff;

        public ParamsForPython(final Params params, final double tol) {
            this.params = params;
            this.tol = tol;
            this.machineEff = params.getMachineEfficiency();
        }

        public double getTolerance() {
            return this.tol;
        }

        public double getMachineEfficiency() {
            return this.machineEff;
        }

        public ImmutableList<Double> getDemandRates() {
            return this.params.getDemandRates();
        }

        public ImmutableList<Double> getProductionRates() {
            return this.params.getProductionRates();
        }

        public ImmutableList<Double> getInventoryHoldingCosts() {
            return this.params.getInventoryHoldingCosts();
        }

        public ImmutableList<Double> getBacklogCosts() {
            return this.params.getBacklogCosts();
        }

    }

}
