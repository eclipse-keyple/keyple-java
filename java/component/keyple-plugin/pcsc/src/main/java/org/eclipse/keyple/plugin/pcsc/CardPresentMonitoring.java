package org.eclipse.keyple.plugin.pcsc;

import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.plugin.local.MonitoringJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class CardPresentMonitoring implements MonitoringJob {

    private static final Logger logger = LoggerFactory.getLogger(CardPresentMonitoring.class);

    private final long waitTimeout;
    private final boolean monitorInsertion;
    private final SeReader reader;
    final private AtomicBoolean loop = new AtomicBoolean() ;

    /**
     * Build a monitoring job to detect the card insertion
     * @param reader : reader that will be polled with the method isSePresent()
     * @param waitTimeout : wait time during two hit of the polling
    *   @param monitorInsertion : if true, polls for SE_INSERTED, else SE_REMOVED
     */
    CardPresentMonitoring(SeReader reader, long waitTimeout, boolean monitorInsertion){
        this.waitTimeout = waitTimeout;
        this.reader = reader;
        this.monitorInsertion = monitorInsertion;
        loop.set(true);
    }

    @Override
    public Runnable getMonitoringJob(final AbstractObservableState state) {
        return new Runnable() {
            long retries = 0;

            @Override
            public void run() {
                //start looping
                loop.set(true);

                logger.debug("[{}] Polling from isSePresentPing", reader.getName());
                while (loop.get()) {
                    try {
                        //polls for SE_INSERTED
                        if (monitorInsertion && reader.isSePresent()) {
                            logger.debug("[{}] The SE is present ", reader.getName());
                            loop.set(false);
                            state.onEvent(AbstractObservableLocalReader.InternalEvent.SE_INSERTED);
                            return;
                        }
                        //polls for SE_REMOVED
                        if (!monitorInsertion && !reader.isSePresent()) {
                            logger.debug("[{}] The SE is not present ", reader.getName());
                            loop.set(false);
                            state.onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);
                            return;
                        }

                    } catch (KeypleIOReaderException e) {
                        loop.set(false);
                        //what do do here
                    }
                    retries++;

                    if (logger.isTraceEnabled()) {
                        logger.trace("[{}] isSePresent polling retries : {}", reader.getName(), retries);
                    }
                    try {
                        // wait a bit
                        Thread.sleep(waitTimeout);
                    } catch (InterruptedException ignored) {
                        // Restore interrupted state...      
                        Thread.currentThread().interrupt();
                        loop.set(false);
                    }
                }
                logger.trace("[{}] Looping has been stopped", reader.getName());

            }
        };
    }

    public void stop(){
        logger.debug("[{}] Stop polling ", reader.getName());
        loop.set(false);
    }

}
