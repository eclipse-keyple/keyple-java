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
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcscReaderImpl_EventTest extends CoreBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(PcscReaderImpl_EventTest.class);


    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }

    @Test
    @Ignore
    public void testInsertRemoveCard() throws KeypleReaderException, InterruptedException {
        logger.info("** ******************************* **");
        logger.info("** Remove any card before the test **");
        logger.info("** ******************************* **");


        final CountDownLatch insert = new CountDownLatch(1);
        final CountDownLatch remove = new CountDownLatch(1);

        PcscPluginImpl plugin = PcscPluginImpl.getInstance();
        PcscReader reader = (PcscReader) plugin.getReaders().first();
        logger.info("Working this reader [{}]", reader.getName());

        reader.addObserver(onInsertedCountDown(insert));
        reader.addObserver(onRemovedCountDown(remove));

        reader.startSeDetection(ObservableReader.PollingMode.REPEATING);
        logger.info("[{}] Waiting 10 seconds for the card insertion...", reader.getName());
        insert.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(0, insert.getCount());
        logger.info("[{}] Card Inserted.", reader.getName());

        logger.info("[{}] Notifying the end of the card processing", reader.getName());
        logger.info("[{}] Waiting 10 seconds for the card removal...", reader.getName());

        // reader.notifySeProcessed();
        remove.await(10, TimeUnit.SECONDS);

        Assert.assertEquals(0, remove.getCount());
        logger.info("[{}] Card removed.", reader.getName());
    }

    @Test
    @Ignore
    public void testAlreadyInsertedCard() throws KeypleReaderException, InterruptedException {
        logger.info("** ***************************** **");
        logger.info("** Insert a card before the test **");
        logger.info("** ***************************** **");

        final CountDownLatch insert = new CountDownLatch(1);
        final CountDownLatch remove = new CountDownLatch(1);

        PcscPluginImpl plugin = PcscPluginImpl.getInstance();
        PcscReader reader = (PcscReader) plugin.getReaders().first();
        logger.info("Working this reader [{}]", reader.getName());

        reader.addObserver(onInsertedCountDown(insert));
        reader.addObserver(onRemovedCountDown(remove));

        reader.startSeDetection(ObservableReader.PollingMode.REPEATING);
        logger.info("[{}] Waiting 1 seconds for the card insertion...", reader.getName());

        insert.await(1, TimeUnit.SECONDS);
        Assert.assertEquals(0, insert.getCount());
        logger.info("[{}] Card Inserted.", reader.getName());
    }

    @Test
    @Ignore
    public void testLoop() throws KeypleReaderException, InterruptedException {
        logger.info("** ********************************************* **");
        logger.info("** try to present a card 5 times   in 10 seconds **");
        logger.info("** ********************************************* **");

        final CountDownLatch insert = new CountDownLatch(5);

        PcscPluginImpl plugin = PcscPluginImpl.getInstance();

        plugin.addObserver(new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {}
        });

        PcscReader reader = (PcscReader) plugin.getReaders().first();
        logger.info("Working this reader [{}]", reader.getName());

        reader.addObserver(onInsertedCountDown(insert));
        reader.addObserver(onRemovedCountDown(null));

        reader.startSeDetection(ObservableReader.PollingMode.REPEATING);

        insert.await(10, TimeUnit.SECONDS);

        if (insert.getCount() == 0) {
            logger.info("** *** **");
            logger.info("** WIN **");
            logger.info("** *** **");
        } else {
            logger.info("** ***  **");
            logger.info("** LOST **");
            logger.info("** ***  **");
        }

    }


    static public ObservableReader.ReaderObserver onRemovedCountDown(final CountDownLatch lock) {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if (event.getEventType() == ReaderEvent.EventType.SE_REMOVED) {
                    logger.info("[{}] Card Removed.", event.getReaderName());
                    if (lock != null) {
                        lock.countDown();
                    }
                } ;

            }
        };
    }

    static public ObservableReader.ReaderObserver onInsertedCountDown(final CountDownLatch lock) {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if (event.getEventType() == ReaderEvent.EventType.SE_INSERTED) {
                    logger.info("[{}] Card Inserted.", event.getReaderName());
                    if (lock != null) {
                        lock.countDown();
                    }
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
