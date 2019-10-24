package org.eclipse.keyple.core.seproxy.plugin.state;

import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.plugin.AbstractThreadedObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.SmartInsertionReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class ThreadedWaitForSeInsertion extends AbstractObservableState {

    private Future<Boolean> waitForCarPresent;
    private final long timeout;

    /** logger */
    private static final Logger logger =
            LoggerFactory.getLogger(ThreadedWaitForSeInsertion.class);

    public ThreadedWaitForSeInsertion(AbstractThreadedObservableLocalReader reader, long timeout) {
        super(MonitoringState.WAIT_FOR_SE_INSERTION, reader);
        this.timeout = timeout;
    }

    @Override
    protected void onEvent(AbstractObservableLocalReader.StateEvent event) {
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
    protected void activate() {
        logger.debug("Activate currentState {} ",this.state);
        waitForCarPresent = ((AbstractThreadedObservableLocalReader)reader).
                getExecutorService().submit(waitForCardPresent(this.timeout));
        //logger.debug("End of activate currentState {} ",this.currentState);

    }

    private Callable<Boolean> waitForCardPresent(final long timeout){
        return new Callable<Boolean>(){
            @Override
            public Boolean call() {
                logger.trace("Invoke waitForCardPresent asynchronously");
                if (((SmartInsertionReader) reader).waitForCardPresent(timeout)) {
                    reader.switchState(MonitoringState.WAIT_FOR_SE_PROCESSING);
                    return true;
                }
                return false;
            }
        };
    }


    @Override
    protected void deActivate() {
        if(waitForCarPresent !=null && !waitForCarPresent.isDone()){
            waitForCarPresent.cancel(true);
        }
    }


}
