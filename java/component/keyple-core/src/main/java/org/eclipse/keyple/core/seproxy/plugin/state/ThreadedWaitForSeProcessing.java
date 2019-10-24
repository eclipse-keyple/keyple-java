package org.eclipse.keyple.core.seproxy.plugin.state;

import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedWaitForSeProcessing extends AbstractObservableState {

    /** logger */
    private static final Logger logger =
            LoggerFactory.getLogger(ThreadedWaitForSeProcessing.class);

    public ThreadedWaitForSeProcessing(AbstractObservableLocalReader reader) {
        super(MonitoringState.WAIT_FOR_SE_PROCESSING, reader);
    }

    @Override
    protected void onEvent(AbstractObservableLocalReader.StateEvent event) {
        logger.trace("Event {} received on reader {} in currentState {}", event, reader.getName(), state);
        switch (event){
            case SE_PROCESSED:
                logger.debug("Se Processed event received for reader {}", reader.getName());
                if (this.reader.getCurrentPollingMode() == ObservableReader.PollingMode.CONTINUE) {
                    this.reader.switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
                }else{
                    // We close the channels now and notify the application of
                    // the SE_REMOVED event.
                    this.reader.processSeRemoved();
                    this.reader.switchState(MonitoringState.WAIT_FOR_START_DETECTION);
                }
                break;
        }
    }
/*
    @Override
    protected void onSeProcessed() {
        logger.debug("Se Processed event received for reader {}", reader.getName());
        if (this.reader.getCurrentPollingMode() == ObservableReader.PollingMode.CONTINUE) {
            this.reader.switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
        }else{
            // We close the channels now and notify the application of
            // the SE_REMOVED event.
            this.reader.processSeRemoved();
            this.reader.switchState(MonitoringState.WAIT_FOR_START_DETECTION);
        }
    }



    @Override
    protected void onStartDetection() {
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
