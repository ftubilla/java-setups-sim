package discreteEvent;

public abstract class AbstractScheduleTrigger implements IScheduleTrigger {

	protected static int count=0;
	
	private int id;
	
	public AbstractScheduleTrigger(){
		this.id = count++;
	}
	
	
	@Override
	public abstract void trigger(Event eventAdded);

	@Override
	public int getId() {
		return this.id;
	}

}
