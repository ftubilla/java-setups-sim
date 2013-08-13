package policies;

import sim.*;

public interface IPolicy {
	
	public void setUpPolicy(Sim sim);
	public void updateControl(Sim sim);
	
}
