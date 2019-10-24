package org.eclipse.keyple.core.seproxy.plugin.state;

import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class ThreadedWaitForSeRemoval extends AbstractObservableState {

    /** logger */
    private static final Logger logger =
            LoggerFactory.getLogger(ThreadedWaitForSeRemoval.class);

    private Future<Boolean> waitForCardAbsentPing;
    private Future<Boolean> waitForCardAbsent;
    private final long timeout;

    public ThreadedWaitForSeRemoval(AbstractObservableLocalReader reader, long timeout) {
        super(MonitoringState.WAIT_FOR_SE_REMOVAL, reader);
        this.timeout = timeout;
    }

    @Override
    protected void onEvent(AbstractObservableLocalReader.StateEvent event) {
        logger.trace("Event {} received on reader {} in currentState {}", event, reader.getName(), state);
        switch (event){
            case SE_REMOVED:
                // the SE has been removed, we return to the currentState of waiting
                // for insertion
                // We notify the application of the SE_REMOVED event.
                reader.processSeRemoved();
                // exit loop
                logger.debug("Se Removed event received for reader {}", reader.getName());
                reader.switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
                break;


        }
    }

/*
    @Override
    protected void onSeRemoved() {
        // the SE has been removed, we return to the currentState of waiting
        // for insertion
        // We notify the application of the SE_REMOVED event.
        reader.processSeRemoved();
        // exit loop
        logger.debug("Se Removed event received for reader {}", reader.getName());
        reader.switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
    }
*/

    @Override
    protected void activate() {
        try {
            if(reader instanceof SmartPresenceReader){
            waitForCardAbsent = waitForCardAbsent(timeout);
                if(waitForCardAbsent.get()){//timeout is already managed within the future
                    onEvent(AbstractObservableLocalReader.StateEvent.SE_REMOVED);
                }else{
                    //se was not removed within timeout
                };

            }else{
                //reader is not instanceof SmartPresenceReader
                //poll card with isPresentPing
                waitForCardAbsentPing = waitForCardAbsentPing();
                if(waitForCardAbsentPing.get(this.timeout, TimeUnit.MILLISECONDS)){
                    onEvent(AbstractObservableLocalReader.StateEvent.SE_REMOVED);
                }else{
                    //se was not removed within timeout
                };
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            //se was not removed within timeout

        }
    }

    /**
     * Invoke waitForCardAbsent
     * @return true is the card was removed
     */
    private Future<Boolean> waitForCardAbsent(final long timeout){
        return ((AbstractThreadedObservableLocalReader)reader).getExecutorService().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return ((SmartPresenceReader) reader)
                        .waitForCardAbsentNative(
                                timeout);

            }
        });
    }

    /**
     * Loop on the isSePresentPing method until the SE is removed or timeout is reached
     * @return true is the card was removed
     */
    private Future<Boolean> waitForCardAbsentPing(){
        return ((AbstractThreadedObservableLocalReader)reader).getExecutorService().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                while(true){
                    if(!reader.isSePresentPing()){
                        return true;
                    }
                    //wait a bit
                    Thread.sleep(10);
                }
            }
        });
    }


    @Override
    protected void deActivate() {
        if(waitForCardAbsent!=null && !waitForCardAbsent.isDone()){
            waitForCardAbsent.cancel(true);
        }
        if(waitForCardAbsentPing!=null && !waitForCardAbsentPing.isDone()){
            waitForCardAbsentPing.cancel(true);
        }
    }

/*
    @Override
    protected void onStartDetection() {
        logger.debug("Start Detection event received for reader {}", reader.getName());
        reader.switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
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
    */
}
