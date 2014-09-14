package sim;

import lombok.extern.apachecommons.CommonsLog;
import metrics.Metrics;
import output.Recorders;
import params.Params;
import policies.IPolicy;
import processes.demand.IDemandProcess;
import processes.generators.IRandomTimeIntervalGenerator;
import processes.production.IProductionProcess;
import system.Machine;
import discreteEvent.ControlEvent;
import discreteEvent.Event;
import discreteEvent.Failure;
import discreteEvent.ListenersCoordinator;
import discreteEvent.MasterScheduler;

@CommonsLog
public class Sim {

	private static int sims=0;
	
	private int id;
	private final Params params;
	private final MasterScheduler masterScheduler;
	private final ListenersCoordinator listenersCoordinator;
	private ProgressBar bar;
	private IDemandProcess demandProcess;
	private IProductionProcess productionProcess;
	private IRandomTimeIntervalGenerator theFailuresGenerator;
	private IRandomTimeIntervalGenerator theRepairsGenerator;
	private Event latestEvent;
	private Machine machine;
	private IPolicy policy;
	private Metrics metrics;
	private Recorders recorders;
	private Clock clock;
	
	public static final double SURPLUS_TOLERANCE = 1e-6;

	public Sim(Params params) {
		id = sims++;		
		log.info("Creating "+this);
		this.clock = new Clock(params.getMetricsStartTime());
		this.masterScheduler = new MasterScheduler(this);
		this.listenersCoordinator = new ListenersCoordinator();
		this.params = params;
	}

	/**
	 * Runs the simulation. If verbose, it displays a progress bar.
	 * 
	 * @param verbose
	 */
	public void run(boolean verbose) {
		
		//Schedule the first failure and set a control event at that time
		Event firstFailure = new Failure(getTime() + getTheFailuresGenerator().nextTimeInterval());		
		this.getMasterScheduler().addEvent(firstFailure);
		this.getMasterScheduler().addEvent(new ControlEvent(this.getTime()));
		bar = new ProgressBar(5, getParams().getFinalTime());

		// Main Loop of the Sim
		while(continueSim()){
			
			log.trace("Sim time: " + getTime());
			if (verbose) {
				bar.setProgress(getTime());
				bar.display();
			}			
											
			//Process the next event
			try {
				getNextEvent().handle(this);
			} catch (NullPointerException e){
				log.fatal("Event returned was null!");
				e.printStackTrace();
				System.exit(-1);
			}

		}
		
		if (verbose) {
			bar.setProgress(getTime());
			bar.display();
		}
				
		getRecorders().recordEndOfSim(this);
	}
	
	public boolean continueSim() {
		return (clock.getTime() < params.getFinalTime() && !eventsComplete());
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

	@Override
	public String toString() {
		return "Sim:"+id;
	}
	
	public String toStringVerbose() {
		return String.format("Sim %d with parameters:\n%s", id, params);		
	}

	public Params getParams() {
		return params;
	}

	public void setTime(double newTime) {
		clock.advanceClockTo(newTime);
	}

	public double getTime() {
		return clock.getTime();
	}
	
	public Clock getClock(){
		return clock;
	}

	public MasterScheduler getMasterScheduler(){
		return masterScheduler;
	}

	public ListenersCoordinator getListenersCoordinator(){
		return listenersCoordinator;
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

	public boolean hasDiscreteMaterial(){
		//We cannot have mixed continuous and discrete processes, but just in case I check with OR.
		return productionProcess.isDiscrete() || demandProcess.isDiscrete();
	}
	
	public void setProductionProcess(IProductionProcess productionProcess) {
		this.productionProcess = productionProcess;
		this.machine.setProductionProcess(productionProcess);
	}
	
	public int getId(){
		return id;
	}
	
	public boolean isTimeToRecordData() {
		return clock.isTimeToRecordData();
	}

}
