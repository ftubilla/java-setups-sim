package optimization;

/**
 * Encapsulates a JOptimizer variable
 * 
 * @author ftubilla
 *
 */
public class OptimizationVar {

	private final String name;
	private double initialValue;
	private Double solution;
	
	public OptimizationVar(String name) {
		this.name = name;		
		this.initialValue = 0.0;
		this.solution = null;
	}	
	
	public void setInitialValue(double val){
		initialValue = val;
	}
	
	public double getInitialValue() {
		return initialValue;
	}
	
	public Double getSol() {
		return solution;
	}
	
	void setSol(double solution){
		this.solution = solution;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}


