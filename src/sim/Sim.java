package sim;

import metrics.Metrics;
import output.Recorders;
import policies.IPolicy;
import processes.demand.IDemandProcess;
import processes.generators.IRandomTimeIntervalGenerator;
import processes.production.IProductionProcess;
import system.Machine;
import discreteEvent.Event;
import discreteEvent.MasterScheduler;

public class Sim {

	private Params params;
	private MasterScheduler masterScheduler;
	private IDemandProcess demandProcess;
	private IProductionProcess productionProcess;
	private IRandomTimeIntervalGenerator theFailuresGenerator;
	private IRandomTimeIntervalGenerator theRepairsGenerator;
	private Event latestEvent;
	private Machine machine;
	private IPolicy policy;
	private Metrics metrics;
	private Recorders recorders;
	private double time = 0.0;

	private static double staticTimeCopy = 0.0;
	public static final double SURPLUS_TOLERANCE = 1e-6;
	public static final boolean DEBUG = false;
	public static boolean TIME_TO_START_RECORDING = false;
	public static double METRICS_INITIAL_TIME = 0.0;

	public Sim() {
		this.masterScheduler = new MasterScheduler(this);
	}

	public boolean continueSim() {
		return (time < params.getFinalTime() && !eventsComplete());
	}

	public IRandomTimeIntervalGenerator getTheFailuresGenerator() {
		return theFailuresGenerator;
	}

	public void setTheFailuresGenerator(
			IRandomTimeIntervalGenerator theFailuresGenerator) {
		this.theFailuresGenerator = theFailuresGenerator;
	}

	public IRandomTimeIntervalGenerator getTheRepairsGenerator() {
		return theRepairsGenerator;
	}

	public void setTheRepairsGenerator(
			IRandomTimeIntervalGenerator theRepairsGenerator) {
		this.theRepairsGenerator = theRepairsGenerator;
	}

	public String toString() {
		String output = "";
		output += "System with the following parameters:\n" + "Demand Rates: "
				+ this.getParams().getDemandRates() + "\n"
				+ "Production Rates: " + this.getParams().getProductionRates()
				+ "\n" + "MTTF: " + this.getParams().getMeanTimeToFail() + "\n"
				+ "MTTR: " + this.getParams().getMeanTimeToRepair() + "\n"
				+ "Setup times: " + this.getParams().getSetupTimes() + "\n"
				+ "Initial setup: " + this.getParams().getInitialSetup();

		return (output);
	}

	public Params getParams() {
		return params;
	}

	public void setParams(Params params) {
		this.params = params;
	}

	public void setTime(double newTime) {
		this.time = newTime;
		staticTimeCopy = this.time;
	}

	public double getTime() {
		//The current time can also be read statically (see static method below)
		return this.time;
	}

	public MasterScheduler getMasterScheduler(){
		return masterScheduler;
	}

	public boolean eventsComplete() {
		return masterScheduler.eventsComplete();
	}

	public Event getNextEvent() {
		return masterScheduler.getNextEvent();
	}

	public Event getLatestEvent() {
		return latestEvent;
	}

	public void setLatestEvent(Event latestEvent) {
		this.latestEvent = latestEvent;
	}

	public Machine getMachine() {
		return machine;
	}

	public void setMachine(Machine machine) {
		this.machine = machine;
	}

	public IPolicy getPolicy() {
		return policy;
	}

	public void setPolicy(IPolicy policy) {
		this.policy = policy;
	}

	public Metrics getMetrics() {
		return metrics;
	}

	public void setMetrics(Metrics theMetrics) {
		this.metrics = theMetrics;
	}

	public Recorders getRecorders() {
		return recorders;
	}

	public void setRecorders(Recorders recorders) {
		this.recorders = recorders;
	}

	public IDemandProcess getDemandProcess(){
		return demandProcess;
	}
	
	public void setDemandProcess(IDemandProcess demandProcess){
		this.demandProcess=demandProcess;
	}

	public IProductionProcess getProductionProcess() {
		return productionProcess;
	}

	public void setProductionProcess(IProductionProcess productionProcess) {
		this.productionProcess = productionProcess;
		this.machine.setProductionProcess(productionProcess);
	}
	
	public static double time(){
		return staticTimeCopy;
	}
}
