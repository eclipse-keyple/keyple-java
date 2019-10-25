package org.eclipse.keyple.plugin.pcsc;

import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PcscReaderImplTest extends CoreBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(PcscReaderImplTest.class);


    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }



    @Test
    public void testDetectCard() throws KeypleReaderException, InterruptedException {
        final CountDownLatch insert = new CountDownLatch(1);
        final CountDownLatch remove = new CountDownLatch(1);

        PcscPluginImpl plugin = PcscPluginImpl.getInstance();
        PcscReader reader = (PcscReader) plugin.getReaders().first();

        reader.addObserver(onInsertedCountDown(insert));
        reader.addObserver(onMatchedCountDown(insert));
        reader.addObserver(onRemovedCountDown(remove));

        reader.startSeDetection(ObservableReader.PollingMode.CONTINUE);

        logger.info("Waiting 10 seconds for a card...");
        insert.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(0, insert.getCount());
        logger.info("Card Inserted.");

        logger.info("Notifying automatically of the processing of the card");
        logger.info("Waiting 10 seconds to remove the card...");

        reader.notifySeProcessed();
        remove.await(10, TimeUnit.SECONDS);

        Assert.assertEquals(0, remove.getCount());
        logger.info("Card removed.");


    }

    static public ObservableReader.ReaderObserver onRemovedCountDown(final CountDownLatch lock) {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if (event.getEventType() == ReaderEvent.EventType.SE_REMOVED) {
                    lock.countDown();
                } ;

            }
        };
    }

    static public ObservableReader.ReaderObserver onInsertedCountDown(final CountDownLatch lock) {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if (event.getEventType() == ReaderEvent.EventType.SE_INSERTED) {
                    lock.countDown();
                } ;

            }
        };
    }

    static public ObservableReader.ReaderObserver onMatchedCountDown(final CountDownLatch lock) {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if (event.getEventType() == ReaderEvent.EventType.SE_MATCHED) {
                    lock.countDown();
                } ;

            }
        };
    }
}
