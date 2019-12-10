package org.eclipse.keyple.plugin.pcsc;

import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.plugin.local.MonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.local.monitoring.CardAbsentPingMonitoringJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class CardPresentMonitoring implements MonitoringJob {

    private static final Logger logger = LoggerFactory.getLogger(CardPresentMonitoring.class);


    private final SeReader reader;
    final private AtomicBoolean loop = new AtomicBoolean() ;

    CardPresentMonitoring(SeReader reader){

        this.reader = reader;
        loop.set(true);
    }

    @Override
    public Runnable getMonitoringJob(final AbstractObservableState state) {
        return new Runnable() {
            long threshold = 500;
            long retries = 0;

            @Override
            public void run() {
                logger.debug("[{}] Polling from isSePresentPing", reader.getName());
                while (loop.get()) {
                    try {
                        if (reader.isSePresent()) {
                            logger.debug("[{}] The SE has been inserted ", reader.getName());
                            loop.set(false);
                            state.onEvent(AbstractObservableLocalReader.InternalEvent.SE_INSERTED);
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
                        Thread.sleep(threshold);
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
