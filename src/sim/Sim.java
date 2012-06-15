package sim;


import output.Recorders;
import discreteEvent.*;
import sim.Params;
import system.*;
import system.scheduler.*;
import sim.metrics.*;

public class Sim {

	private Params params;
	private Schedule failuresSchedule;
	private Schedule productionSchedule;
	private IRandomTimeIntervalGenerator theFailuresGenerator;
	private IRandomTimeIntervalGenerator theRepairsGenerator;
	private Event latestEvent;
	private Machine machine;
	private IScheduler theScheduler;
	private Metrics metrics;
	private Recorders recorders;
	private double time;

	public static final double SURPLUS_TOLERANCE = 1e-6;
	public static final boolean DEBUG = false;
	public static boolean TIME_TO_START_RECORDING = false;
	public static double METRICS_INITIAL_TIME = 0.0; 



	public Sim(){
		this.time = 0.0;
		this.failuresSchedule = new Schedule(/*dumpable=*/false);
		this.productionSchedule = new Schedule(/*dumpable=*/true);
	}
	
	public boolean continueSim(){
		if (time < params.getFinalTime() && !eventsComplete()){
			return true;
		}else{
			return false;
		}
	}
	
	public IRandomTimeIntervalGenerator getTheFailuresGenerator() {
		return theFailuresGenerator;
	}



	public void setTheFailuresGenerator(IRandomTimeIntervalGenerator theFailuresGenerator) {
		this.theFailuresGenerator = theFailuresGenerator;
	}



	public IRandomTimeIntervalGenerator getTheRepairsGenerator() {
		return theRepairsGenerator;
	}



	public void setTheRepairsGenerator(IRandomTimeIntervalGenerator theRepairsGenerator) {
		this.theRepairsGenerator = theRepairsGenerator;
	}



	public String toString(){
		String output = "";
		output += "System with the following parameters:\n"
				+ "Demand Rates: " + this.getParams().getDemandRates() + "\n"
				+ "Production Rates: " + this.getParams().getProductionRates() + "\n"
				+ "MTTF: " + this.getParams().getMeanTimeToFail() + "\n"
				+ "MTTR: " + this.getParams().getMeanTimeToRepair() + "\n" 
				+ "Setup times: " + this.getParams().getSetupTimes() + "\n"
				+ "Initial setup: " + this.getParams().getInitialSetup();
		
		return(output);
	}
	
	
	
	public Params getParams() {
		return params;
	}

	public void setParams(Params params) {
		this.params = params;
	}
	
	
	public void setTime(double newTime){
		this.time = newTime;
	}
	
	public double getTime(){
		return this.time;
	}


	public Schedule getFailuresSchedule() {
		return failuresSchedule;
	}


	public Schedule getProductionSchedule() {
		return productionSchedule;
	}
	
	public boolean eventsComplete(){
		if (productionSchedule.eventsComplete() && failuresSchedule.eventsComplete()){
			return true;
		}
		else{
			return false;
		}
	}
	
	public Event getNextEvent(){
		if (eventsComplete()){
			return null;
		}
		if (productionSchedule.eventsComplete()){
			return failuresSchedule.getNextEvent();
		}
		if (failuresSchedule.eventsComplete()){
			return productionSchedule.getNextEvent();
		}
		if (productionSchedule.nextEventTime() <= failuresSchedule.nextEventTime()){
			return productionSchedule.getNextEvent();
		} else {
			return failuresSchedule.getNextEvent();
		}
		
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

	public IScheduler getTheScheduler() {
		return theScheduler;
	}

	public void setTheScheduler(IScheduler theScheduler) {
		this.theScheduler = theScheduler;
	}

	public Metrics getMetrics() {
		return metrics;
	}
	
	public void setMetrics(Metrics theMetrics){
		this.metrics = theMetrics;
	}

	public Recorders getRecorders() {
		return recorders;
	}

	public void setRecorders(Recorders recorders) {
		this.recorders = recorders;
	}
	
}
