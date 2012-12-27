package discreteEvent;

public class BeforeEventListener implements IEventListener {

	private static int count=0;
	
	private int id;
	
	public BeforeEventListener(){
		id=count++;
	}
	
	@Override
	public void execute(Event event) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getId(){
		return id;
	}
}
