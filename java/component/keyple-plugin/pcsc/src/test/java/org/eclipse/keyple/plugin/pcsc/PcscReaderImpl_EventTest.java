/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.pcsc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This test suite helps testing a pcsc reader with real sc */
@Ignore
public class PcscReaderImpl_EventTest extends CoreBaseTest {

  private static final Logger logger = LoggerFactory.getLogger(PcscReaderImpl_EventTest.class);

  @Before
  public void setUp() {
    logger.info("------------------------------");
    logger.info("Test {}", name.getMethodName() + "");
    logger.info("------------------------------");
  }

  @Test
  @Ignore // needs a reader
  public void testInsertRemoveCard() throws InterruptedException {
    logger.info("** ******************************* **");
    logger.info("** Remove any card before the test **");
    logger.info("** ******************************* **");

    final CountDownLatch insert = new CountDownLatch(1);
    final CountDownLatch remove = new CountDownLatch(1);

    PcscPluginImpl plugin = PcscPluginImpl.getInstance();
    PcscReader reader = (PcscReader) plugin.getReaders().values().toArray()[0];
    logger.info("Working this reader [{}]", reader.getName());

    reader.addObserver(onInsertedCountDown(insert));
    reader.addObserver(onRemovedCountDown(remove));

    reader.startCardDetection(ObservableReader.PollingMode.REPEATING);
    logger.info("[{}] Waiting 10 seconds for the card insertion...", reader.getName());
    insert.await(10, TimeUnit.SECONDS);
    Assert.assertEquals(0, insert.getCount());
    logger.info("[{}] Card Inserted.", reader.getName());

    logger.info("[{}] Notifying the end of the card processing", reader.getName());
    logger.info("[{}] Waiting 10 seconds for the card removal...", reader.getName());

    remove.await(10, TimeUnit.SECONDS);

    Assert.assertEquals(0, remove.getCount());
    logger.info("[{}] Card removed.", reader.getName());
  }

  @Test
  @Ignore // needs a reader
  public void testAlreadyInsertedCard() throws InterruptedException {
    logger.info("** ***************************** **");
    logger.info("** Insert a card before the test **");
    logger.info("** ***************************** **");

    final CountDownLatch insert = new CountDownLatch(1);
    final CountDownLatch remove = new CountDownLatch(1);

    PcscPluginImpl plugin = PcscPluginImpl.getInstance();
    PcscReader reader = (PcscReader) plugin.getReaders().values().toArray()[0];
    logger.info("Working this reader [{}]", reader.getName());

    reader.addObserver(onInsertedCountDown(insert));
    reader.addObserver(onRemovedCountDown(remove));

    reader.startCardDetection(ObservableReader.PollingMode.REPEATING);
    logger.info("[{}] Waiting 1 seconds for the card insertion...", reader.getName());

    insert.await(1, TimeUnit.SECONDS);
    Assert.assertEquals(0, insert.getCount());
    logger.info("[{}] Card Inserted.", reader.getName());
  }

  @Test
  @Ignore // needs a reader
  public void testLoop() throws InterruptedException {
    logger.info("** ********************************************* **");
    logger.info("** try to present a card 5 times   in 10 seconds **");
    logger.info("** ********************************************* **");

    final CountDownLatch insert = new CountDownLatch(5);

    PcscPluginImpl plugin = PcscPluginImpl.getInstance();

    plugin.addObserver(
        new ObservablePlugin.PluginObserver() {
          @Override
          public void update(PluginEvent event) {}
        });

    PcscReader reader = (PcscReader) plugin.getReaders().values().toArray()[0];
    logger.info("Working this reader [{}]", reader.getName());

    reader.addObserver(onInsertedCountDown(insert));
    reader.addObserver(onRemovedCountDown(null));

    reader.startCardDetection(ObservableReader.PollingMode.REPEATING);

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

  @Test
  @Ignore // needs a reader
  public void fullScenario() throws InterruptedException {

    final CountDownLatch connect1 = new CountDownLatch(1);
    final CountDownLatch disconnect1 = new CountDownLatch(1);
    final CountDownLatch insert = new CountDownLatch(1);
    final CountDownLatch remove = new CountDownLatch(1);
    final CountDownLatch connect2 = new CountDownLatch(1);
    final CountDownLatch insert2 = new CountDownLatch(1);
    final CountDownLatch remove2 = new CountDownLatch(1);

    PcscPluginImpl plugin = PcscPluginImpl.getInstance();

    // start with no reader connected
    assert plugin.getReaders().isEmpty() : "Disconnect all readers before this test";

    plugin.addObserver(onReaderConnected(plugin, connect1, insert, remove));
    plugin.addObserver(onReaderDisconnected(disconnect1));

    logger.info("** ************************");
    logger.info("** Connect a PCSC Reader **");
    logger.info("** ********************* **");

    connect1.await(10, TimeUnit.SECONDS);

    logger.info("** ************************");
    logger.info("** Present a card        **");
    logger.info("** ********************* **");

    insert.await(10, TimeUnit.SECONDS);

    logger.info("** ************************");
    logger.info("** Remove the card       **");
    logger.info("** ********************* **");

    remove.await(10, TimeUnit.SECONDS);

    logger.info("** **************  ********** **");
    logger.info("** Disconnect the PCSC Reader **");
    logger.info("** ****************  ******** **");

    disconnect1.await(10, TimeUnit.SECONDS);
    plugin.clearObservers();
    plugin.addObserver(onReaderConnected(plugin, connect2, insert2, remove2));

    logger.info("** ********************* **");
    logger.info("** Connect a PCSC Reader **");
    logger.info("** ********************* **");

    connect2.await(10, TimeUnit.SECONDS);

    logger.info("** ************************");
    logger.info("** Present a card        **");
    logger.info("** ********************* **");

    insert2.await(10, TimeUnit.SECONDS);
  }

  /*
   * HELPERS
   */

  public static ObservablePlugin.PluginObserver onReaderConnected(
      final PcscPlugin plugin,
      final CountDownLatch connectedLock,
      final CountDownLatch insertedlock,
      final CountDownLatch removedlock) {
    return new ObservablePlugin.PluginObserver() {
      @Override
      public void update(PluginEvent event) {
        // only one reader should be connected
        Assert.assertEquals(1, event.getReaderNames().size());
        if (event.getEventType() == PluginEvent.EventType.READER_CONNECTED) {
          logger.info("[{}] Reader connected.", event.getReaderNames().first());
          if (connectedLock != null) {
            PcscReader reader = (PcscReader) plugin.getReaders().values().toArray()[0];
            reader.addObserver(onInsertedCountDown(insertedlock));
            reader.addObserver(onRemovedCountDown(removedlock));
            reader.startCardDetection(ObservableReader.PollingMode.REPEATING);
            connectedLock.countDown();
          }
        }
        ;
      }
    };
  }

  public static ObservablePlugin.PluginObserver onReaderDisconnected(final CountDownLatch lock) {
    return new ObservablePlugin.PluginObserver() {
      @Override
      public void update(PluginEvent event) {
        if (event.getEventType() == PluginEvent.EventType.READER_DISCONNECTED) {
          logger.info("[{}] Reader connected.", event.getReaderNames().first());
          if (lock != null) {
            lock.countDown();
          }
        }
        ;
      }
    };
  }

  public static ObservableReader.ReaderObserver onRemovedCountDown(final CountDownLatch lock) {
    return new ObservableReader.ReaderObserver() {
      @Override
      public void update(ReaderEvent event) {
        if (event.getEventType() == ReaderEvent.EventType.CARD_REMOVED) {
          logger.info("[{}] Card Removed.", event.getReaderName());
          if (lock != null) {
            lock.countDown();
          }
        }
        ;
      }
    };
  }

  public static ObservableReader.ReaderObserver onInsertedCountDown(final CountDownLatch lock) {
    return new ObservableReader.ReaderObserver() {
      @Override
      public void update(ReaderEvent event) {
        if (event.getEventType() == ReaderEvent.EventType.CARD_INSERTED) {
          logger.info("[{}] Card Inserted.", event.getReaderName());
          if (lock != null) {
            lock.countDown();
          }
        }
        ;
      }
    };
  }

  public static ObservableReader.ReaderObserver onMatchedCountDown(final CountDownLatch lock) {
    return new ObservableReader.ReaderObserver() {
      @Override
      public void update(ReaderEvent event) {
        if (event.getEventType() == ReaderEvent.EventType.CARD_MATCHED) {
          lock.countDown();
        }
        ;
      }
    };
  }
}
