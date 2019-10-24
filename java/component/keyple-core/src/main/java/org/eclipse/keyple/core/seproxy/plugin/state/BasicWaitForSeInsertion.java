package org.eclipse.keyple.core.seproxy.plugin.state;

import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.plugin.AbstractThreadedObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.SmartInsertionReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class BasicWaitForSeInsertion extends AbstractObservableState {

    /** logger */
    private static final Logger logger =
            LoggerFactory.getLogger(BasicWaitForSeInsertion.class);

    public BasicWaitForSeInsertion(AbstractObservableLocalReader reader) {
        super(MonitoringState.WAIT_FOR_SE_INSERTION, reader);
    }

    @Override
    protected void onEvent(AbstractObservableLocalReader.StateEvent event) {
        logger.trace("Event {} received on reader {} in currentState {}", event, reader.getName(), state);
        switch (event){
            case SE_INSERTED:
            case SE_MATCHED:
                logger.debug("Se Inserted event received for reader {}", reader.getName());
                this.reader.switchState(MonitoringState.WAIT_FOR_SE_PROCESSING);
                break;

            case STOP_DETECT:
                this.reader.switchState(MonitoringState.WAIT_FOR_START_DETECTION);
                break;
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
    protected void activate() {}


    @Override
    protected void deActivate() {}


}
