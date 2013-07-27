package policies;

import org.apache.log4j.Logger;

public class AbstractPolicy {
	
	private static Logger logger = Logger.getLogger(AbstractPolicy.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
}


