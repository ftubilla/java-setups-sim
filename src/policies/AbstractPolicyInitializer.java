package policies;

import org.apache.log4j.Logger;

public class AbstractPolicyInitializer {
	private static Logger logger = Logger.getLogger(AbstractPolicyInitializer.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
}


