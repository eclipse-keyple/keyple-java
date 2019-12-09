package org.eclipse.keyple.plugin.pcsc;

import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.plugin.local.MonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.local.monitoring.CardAbsentPingMonitoringJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardPresentMonitoring implements MonitoringJob {

    private static final Logger logger = LoggerFactory.getLogger(CardPresentMonitoring.class);


    private final SeReader reader;

    CardPresentMonitoring(SeReader reader){
        this.reader = reader;
    }

    @Override
    public Runnable getMonitoringJob(final AbstractObservableState state) {
        return new Runnable() {
            long threshold = 500;
            long retries = 0;
            boolean loop = true;

            @Override
            public void run() {
                logger.debug("[{}] Polling from isSePresentPing", reader.getName());
                while (loop) {
                    try {
                        if (reader.isSePresent()) {
                            logger.debug("[{}] The SE has been inserted ", reader.getName());
                            loop = false;
                            state.onEvent(AbstractObservableLocalReader.InternalEvent.SE_INSERTED);
                            return;
                        }
                    } catch (KeypleIOReaderException e) {
                        loop = false;
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
                        loop = false;
                    }
                }
            }
        };
    }
}
