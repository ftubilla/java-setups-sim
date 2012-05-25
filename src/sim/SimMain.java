package sim;

public class SimMain {

	static Sim sim;
	
	public static void main(String[] args){
		
		sim = new Sim();
		SimSetup.setup(sim);
		SimRun.run(sim);
		
	}
	
	
}
