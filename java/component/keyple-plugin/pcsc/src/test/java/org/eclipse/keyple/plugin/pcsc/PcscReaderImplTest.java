/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.pcsc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcscReaderImplTest extends CoreBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(PcscReaderImplTest.class);


    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }



    @Test
    @Ignore
    public void testDetectCard() throws KeypleReaderException, InterruptedException {
        final CountDownLatch insert = new CountDownLatch(1);
        final CountDownLatch remove = new CountDownLatch(1);

        PcscPluginImpl plugin = PcscPluginImpl.getInstance();
        PcscReader reader = (PcscReader) plugin.getReaders().first();

        reader.addObserver(onInsertedCountDown(insert));
        reader.addObserver(onRemovedCountDown(remove));

        reader.startSeDetection(ObservableReader.PollingMode.CONTINUE);

        logger.info("Waiting 10 seconds for the card insertion...");
        insert.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(0, insert.getCount());
        logger.info("Card Inserted.");

        logger.info("Notifying the end of the card processing");
        logger.info("Waiting 10 seconds for the card removal...");

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
