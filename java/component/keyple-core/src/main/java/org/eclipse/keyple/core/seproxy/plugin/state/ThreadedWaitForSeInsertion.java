package org.eclipse.keyple.core.seproxy.plugin.state;

import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractThreadedObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.SmartInsertionReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class ThreadedWaitForSeInsertion extends DefaultWaitForSeInsertion {

    private Future<Boolean> waitForCarPresent;
    private final long timeout;

    /** logger */
    private static final Logger logger =
            LoggerFactory.getLogger(ThreadedWaitForSeInsertion.class);

    public ThreadedWaitForSeInsertion(AbstractThreadedObservableLocalReader reader, long timeout) {
        super(reader);
        this.timeout = timeout;
    }


    @Override
    public void activate() {
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
                    onEvent(AbstractObservableLocalReader.InternalEvent.SE_INSERTED);
                    return true;
                }
                onEvent(AbstractObservableLocalReader.InternalEvent.TIME_OUT);
                return false;
            }
        };
    }


    @Override
    public void deActivate() {
        if(waitForCarPresent !=null && !waitForCarPresent.isDone()){
            waitForCarPresent.cancel(true);
        }
    }


}
