package org.eclipse.keyple.core.seproxy.plugin.state;

import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedWaitForStartDetect extends AbstractObservableState {

    /** logger */
    private static final Logger logger =
            LoggerFactory.getLogger(ThreadedWaitForStartDetect.class);

    public ThreadedWaitForStartDetect(AbstractObservableLocalReader reader) {
        super(MonitoringState.WAIT_FOR_START_DETECTION, reader);
    }


    @Override
    protected void onEvent(AbstractObservableLocalReader.StateEvent event) {
        logger.trace("Event {} received on reader {} in currentState {}", event, reader.getName(), state);
        switch (event){
            case START_DETECT:
                reader.switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
                break;

        }
    }

    /*
    @Override
    protected void onStartDetection() {
        logger.debug("Start Detection event received for reader {}", reader.getName());
    }

    @Override
    protected void onStopDetection() {
        logger.debug("Stop Detection event received for reader {}", reader.getName());
    }

    @Override
    protected void onSeInserted() {
        logger.debug("Se Inserted event received for reader {}", reader.getName());
    }

    @Override
    protected void onSeProcessed() {
        logger.debug("Se Processed event received for reader {}", reader.getName());
    }

    @Override
    protected void onSeRemoved() {
        logger.debug("Se Removed event received for reader {}", reader.getName());
    }
*/


    @Override
    protected void activate() {

    }

    @Override
    protected void deActivate() {

    }
}
