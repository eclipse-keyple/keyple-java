package org.eclipse.keyple.core.seproxy.plugin.state;

import org.eclipse.keyple.core.seproxy.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class ThreadedWaitForSeRemoval extends DefaultWaitForSeRemoval {

    /** logger */
    private static final Logger logger =
            LoggerFactory.getLogger(ThreadedWaitForSeRemoval.class);

    private FutureTask<Boolean> waitForCardAbsentPing;
    private FutureTask<Boolean> waitForCardAbsent;
    private final long timeout;

    public ThreadedWaitForSeRemoval(AbstractObservableLocalReader reader, long timeout) {
        super(reader);
        this.timeout = timeout;
    }



    @Override
    public void activate() {
        logger.trace("Activate ThreadedWaitForSeRemoval state");
        ExecutorService executor = ((AbstractThreadedObservableLocalReader)reader).getExecutorService();
        try {
            if(reader instanceof SmartPresenceReader){
                logger.trace("Reader is SmartPresence enabled");
                waitForCardAbsent = waitForCardAbsent(timeout);

                executor.submit(waitForCardAbsent);

                if(waitForCardAbsent.get()){//timeout is already managed within the task
                     onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);
                }else{
                    //se was not removed within timeout
                    onEvent(AbstractObservableLocalReader.InternalEvent.TIME_OUT);
                };

            }else{
                //reader is not instanceof SmartPresenceReader
                //poll card with isPresentPing
                logger.trace("Reader is not SmartPresence enabled");
                waitForCardAbsentPing = waitForCardAbsentPing();

                executor.submit(waitForCardAbsentPing);

                if(waitForCardAbsentPing.get(this.timeout, TimeUnit.MILLISECONDS)){
                    onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);
                }else{
                    //se was not removed within timeout
                    onEvent(AbstractObservableLocalReader.InternalEvent.TIME_OUT);
                };
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            //se was not removed within timeout
            onEvent(AbstractObservableLocalReader.InternalEvent.TIME_OUT);
        }
    }

    /**
     * Invoke waitForCardAbsent
     * @return true is the card was removed
     */
    private FutureTask<Boolean> waitForCardAbsent(final long timeout){
        logger.trace("Using method waitForCardAbsentNative");
        return new FutureTask(new Callable<Boolean>() {
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
    private FutureTask<Boolean> waitForCardAbsentPing(){
        logger.trace("Polling method isSePresentPing");
        return new FutureTask(new Callable<Boolean>() {
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
    public void deActivate() {
        if(waitForCardAbsent!=null && !waitForCardAbsent.isDone()){
            waitForCardAbsent.cancel(true);
        }
        if(waitForCardAbsentPing!=null && !waitForCardAbsentPing.isDone()){
            waitForCardAbsentPing.cancel(true);
        }
    }

}
