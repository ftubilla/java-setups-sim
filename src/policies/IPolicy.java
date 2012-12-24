package policies;

import sim.*;

public interface IPolicy {

	public void setup(Sim sim);
	public void updateControl(Sim sim);
	
}
