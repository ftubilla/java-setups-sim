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
    public double[][] compute(final ProductionSequence sequence, final boolean compensateForEfficiency,
            final double tolerance) throws Exception {

        if ( compensateForEfficiency ) {
            throw new RuntimeException("This feature is unsupported right now!!!");
        }
        
        String dir = System.getProperty("user.dir");
        String pythonPath = SimMain.getProperties().getProperty("python.path");
        String scriptPath = dir + File.separator + "python" + File.separator + "grp.py";

        // Write the params as a JSON object to pass as argument
        ObjectMapper mapper = new ObjectMapper();
        String paramsJson = mapper.writeValueAsString(new ParamsForPython(params, tolerance));

        // Write the sequence
        String sequenceString = sequence.toString();
        ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath, paramsJson, sequenceString, compensateForEfficiency + "");
        Process p = pb.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));  
        BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        // Check for any error output
        final StringBuilder errOutputBuilder = new StringBuilder();
        err.lines().forEach(l -> errOutputBuilder.append(String.format("%s%n", l)));
        String errOutput = errOutputBuilder.toString();
        if ( !errOutput.isEmpty() ) {
            log.warn(String.format("Python script messages:%n%s", errOutput));
            if ( errOutput.contains("TOLERANCE NOT MET") ) {
                throw new Exception("Tolerance not met while solving the ARME");
            }
        }

        // Collect the output into the matrix
        final double[][] G = new double[sequence.getSize()][params.getNumItems()];
        in.lines().forEach(l -> {
            String[] line = l.split(" ");
            int i = Integer.parseInt(line[0]);
            int j = Integer.parseInt(line[1]);
            double g = Double.parseDouble(line[2]);
            G[i][j] = g;
            log.trace(String.format("Gallego G[%d,%d]=%.5f", i, j, g));
        });
        
        
        return G;
    }
    
    /*
     * A wrapper class to expose only the set of parameters that are needed
     * for computing the G matrix in the Python script.
     */
    public static class ParamsForPython {

        private final Params params;
        private final double tol;

        public ParamsForPython(final Params params, final double tol) {
            this.params = params;
            this.tol = tol;
        }

        public double getTolerance() {
            return this.tol;
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
