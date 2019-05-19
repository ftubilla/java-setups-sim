package discreteEvent;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Holds the event listeners, which will be called before or after the event is
 * handled.
 * 
 * 
 * @author ftubilla
 *
 */
public class ListenersCoordinator {

    private static Logger logger = Logger.getLogger(ListenersCoordinator.class);

    @SuppressWarnings("unused") private boolean debug = logger.isDebugEnabled();

    @SuppressWarnings("unused") private boolean trace = logger.isTraceEnabled();

    private List<IEventListener> beforeEventListeners;
    private List<IEventListener> afterEventListeners;

    public ListenersCoordinator() {
        beforeEventListeners = new LinkedList<IEventListener>();
        afterEventListeners = new LinkedList<IEventListener>();
    }

    public void addBeforeEventListener(IEventListener listener) {
        logger.debug("Adding before-event listener " + listener.getId());
        beforeEventListeners.add(listener);
    }

    public void addAfterEventListener(IEventListener listener) {
        logger.debug("Adding after-event listener " + listener.getId());
        afterEventListeners.add(listener);
    }

    public Iterable<IEventListener> getBeforeEventListeners() {
        return beforeEventListeners;
    }

    public Iterable<IEventListener> getAfterEventListener() {
        return afterEventListeners;
    }

}
