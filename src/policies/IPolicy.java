package policies;

import sim.*;

public interface IPolicy {
	
	public void setUp(Sim sim);
	public void updateControl(Sim sim);
	
}
