package org.eclipse.keyple.core.seproxy.plugin.state;

import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultWaitForSeInsertion extends AbstractObservableState {

    /** logger */
    private static final Logger logger =
            LoggerFactory.getLogger(DefaultWaitForSeInsertion.class);

    public DefaultWaitForSeInsertion(AbstractObservableLocalReader reader) {
        super(MonitoringState.WAIT_FOR_SE_INSERTION, reader);
    }

    @Override
    public void onEvent(AbstractObservableLocalReader.InternalEvent event) {
        logger.trace("Event {} received on reader {} in currentState {}", event, reader.getName(), state);
        switch (event){
            case SE_INSERTED:
                logger.debug("Se Inserted event received for reader {}", reader.getName());
                if (this.reader.processSeInserted()) {
                    this.reader.switchState(MonitoringState.WAIT_FOR_SE_PROCESSING);
                }else{
                    this.reader.switchState(MonitoringState.WAIT_FOR_SE_REMOVAL);
                }
                break;

            case STOP_DETECT:
                this.reader.switchState(MonitoringState.WAIT_FOR_START_DETECTION);
                break;

            case TIME_OUT:
                this.reader.switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
                break;

            default:
                logger.trace("ignore event");

        }
    }

    /*
    @Override
    protected void onSeInserted() {
        logger.debug("Se Inserted event received for reader {}", reader.getName());
        if (this.reader.processSeInserted()) {
            this.reader.switchState(MonitoringState.WAIT_FOR_SE_PROCESSING);
        }else{
            this.reader.switchState(MonitoringState.WAIT_FOR_SE_REMOVAL);
        }
    }

    @Override
    protected void onStartDetection() {
        logger.debug("Start Detection event received for reader {}", reader.getName());
    }

    @Override
    protected void onStopDetection() {
        logger.debug("Stop Detection event received for reader {}", reader.getName());

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
    public void activate() {}


    @Override
    public void deActivate() {}


}
