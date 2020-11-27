/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.integration;

import static org.eclipse.keyple.plugin.stub.StubReaderTest.hoplinkSE;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.core.card.message.CardResponse;
import org.eclipse.keyple.core.card.message.DefaultSelectionsResponse;
import org.eclipse.keyple.core.card.message.ProxyReader;
import org.eclipse.keyple.core.card.selection.AbstractCardSelection;
import org.eclipse.keyple.core.card.selection.AbstractSmartCard;
import org.eclipse.keyple.core.card.selection.CardSelectionsResult;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualObservableReader;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubReaderTest;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Test Virtual Reader Service with stub plugin and hoplink card */
@RunWith(Parameterized.class)
@Ignore
public class VirtualReaderEventTest extends VirtualReaderBaseTest {

  private static final Logger logger = LoggerFactory.getLogger(VirtualReaderEventTest.class);

  private VirtualObservableReader virtualReader;
  private StubReader nativeReader;

  static final Integer X_TIMES = 5; // run tests multiple times to reproduce flaky

  @Parameterized.Parameters
  public static Object[][] data() {
    return new Object[X_TIMES][0];
  }

  /*
   * Card EVENTS
   */

  @Before
  public void setUp() throws Exception {
    Assert.assertEquals(0, SmartCardService.getInstance().getPlugins().size());

    initMasterNSlave();

    /*
     * connect stub reader to create virtual reader
     */
    nativeReader = this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID, true);

    // get virtual reader
    virtualReader = (VirtualObservableReader) getVirtualReader();
  }

  @After
  public void tearDown() throws Exception {
    disconnectReader(NATIVE_READER_NAME);

    clearMasterNSlave();

    unregisterPlugins();

    Assert.assertEquals(0, SmartCardService.getInstance().getPlugins().size());
  }

  /**
   * Test CARD_INSERTED Reader Event throwing and catching
   *
   * @throws Exception
   */
  @Test
  public void testInsert() throws Exception {
    // lock test until message is received
    final CountDownLatch lock = new CountDownLatch(1);

    ObservableReader.ReaderObserver obs =
        new ObservableReader.ReaderObserver() {
          @Override
          public void update(ReaderEvent event) {
            Assert.assertEquals(event.getReaderName(), virtualReader.getName());
            Assert.assertEquals(event.getPluginName(), masterAPI.getPlugin().getName());
            Assert.assertEquals(ReaderEvent.EventType.CARD_INSERTED, event.getEventType());
            logger.debug("Reader Event is correct, release lock");
            lock.countDown();
          }
        };

    // register stubPluginObserver
    virtualReader.addObserver(obs);

    nativeReader.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);

    logger.info("Insert a Hoplink card and wait 5 seconds for a card event to be thrown");

    // insert card
    nativeReader.insertSe(StubReaderTest.hoplinkSE());

    // wait 5 seconds
    lock.await(5, TimeUnit.SECONDS);

    nativeReader.stopCardDetection();

    // remove observer
    virtualReader.removeObserver(obs);

    Assert.assertEquals(0, lock.getCount());
  }

  /**
   * Test CARD_REMOVED Reader Event throwing and catching
   *
   * @throws Exception
   */
  @Test
  public void testRemoveEvent() throws Exception {

    // lock test until two messages are received
    final CountDownLatch lock = new CountDownLatch(2);

    ObservableReader.ReaderObserver obs =
        new ObservableReader.ReaderObserver() {
          @Override
          public void update(ReaderEvent event) {
            if (event.getEventType() == ReaderEvent.EventType.CARD_INSERTED) {
              // we expect the first event to be CARD_INSERTED
              Assert.assertEquals(2, lock.getCount());
              lock.countDown();
            } else {
              // the next event should be CARD_REMOVED
              Assert.assertEquals(1, lock.getCount());
              Assert.assertEquals(event.getReaderName(), virtualReader.getName());
              Assert.assertEquals(event.getPluginName(), masterAPI.getPlugin().getName());
              Assert.assertEquals(ReaderEvent.EventType.CARD_REMOVED, event.getEventType());
              logger.debug("Reader Event is correct, release lock");
              lock.countDown();
            }
          }
        };
    // register stubPluginObserver
    virtualReader.addObserver(obs);

    logger.info(
        "Insert and remove a Hoplink card and wait 5 seconds for two card events to be thrown");

    nativeReader.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);

    // insert card
    nativeReader.insertSe(StubReaderTest.hoplinkSE());
    // wait 0,5 second
    Thread.sleep(500);
    ((ProxyReader) nativeReader).releaseChannel();

    // remove card
    nativeReader.removeSe();

    nativeReader.stopCardDetection();

    // wait 5 seconds
    lock.await(5, TimeUnit.SECONDS);

    // remove observer
    virtualReader.removeObserver(obs);

    Assert.assertEquals(0, lock.getCount());
  }

  @Test
  public void testInsertSmartCard() throws InterruptedException {

    // CountDown lock
    final CountDownLatch lock = new CountDownLatch(1);
    final String poAid = "A000000291A000000191";

    ObservableReader.ReaderObserver obs =
        new ObservableReader.ReaderObserver() {
          @Override
          public void update(ReaderEvent event) {
            Assert.assertEquals(event.getReaderName(), virtualReader.getName());
            Assert.assertEquals(event.getPluginName(), masterAPI.getPlugin().getName());
            Assert.assertEquals(ReaderEvent.EventType.CARD_MATCHED, event.getEventType());
            Assert.assertTrue(
                ((DefaultSelectionsResponse) event.getDefaultSelectionsResponse())
                    .getSelectionCardResponses()
                    .get(0)
                    .getSelectionStatus()
                    .hasMatched());

            Assert.assertArrayEquals(
                ((DefaultSelectionsResponse) event.getDefaultSelectionsResponse())
                    .getSelectionCardResponses()
                    .get(0)
                    .getSelectionStatus()
                    .getAtr()
                    .getBytes(),
                hoplinkSE().getATR());

            // retrieve the expected FCI from the card Stub running the select application command
            byte[] aid = ByteArrayUtil.fromHex(poAid);
            byte[] selectApplicationCommand = new byte[6 + aid.length];
            selectApplicationCommand[0] = (byte) 0x00; // CLA
            selectApplicationCommand[1] = (byte) 0xA4; // INS
            selectApplicationCommand[2] = (byte) 0x04; // P1: select by name
            selectApplicationCommand[3] = (byte) 0x00; // P2: requests the first
            selectApplicationCommand[4] = (byte) (aid.length); // Lc
            System.arraycopy(aid, 0, selectApplicationCommand, 5, aid.length); // data

            selectApplicationCommand[5 + aid.length] = (byte) 0x00; // Le
            byte[] fci = null;
            try {
              fci = hoplinkSE().processApdu(selectApplicationCommand);
            } catch (KeypleReaderIOException e) {
              e.printStackTrace();
            }

            Assert.assertArrayEquals(
                ((DefaultSelectionsResponse) event.getDefaultSelectionsResponse())
                    .getSelectionCardResponses()
                    .get(0)
                    .getSelectionStatus()
                    .getFci()
                    .getBytes(),
                fci);

            logger.debug("match event is correct");
            // unlock thread
            lock.countDown();
          }
        };

    // register observer
    virtualReader.addObserver(obs);

    CardSelectionsService cardSelectionsService = new CardSelectionsService();

    GenericCardSelectionRequest genericCardSelectionRequest =
        new GenericCardSelectionRequest(
            CardSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poAid).build())
                .build());

    cardSelectionsService.prepareSelection(genericCardSelectionRequest);

    ((ObservableReader) virtualReader)
        .setDefaultSelectionRequest(
            cardSelectionsService.getDefaultSelectionsRequest(),
            ObservableReader.NotificationMode.MATCHED_ONLY);

    nativeReader.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);

    // wait 1 second
    Thread.sleep(1000);

    // test
    nativeReader.insertSe(StubReaderTest.hoplinkSE());

    // lock thread for 2 seconds max to wait for the event
    lock.await(5, TimeUnit.SECONDS);

    nativeReader.stopCardDetection();

    // remove observer
    virtualReader.removeObserver(obs);

    Assert.assertEquals(0, lock.getCount()); // should be 0 because countDown is called by
    // observer

  }

  @Test
  public void testInsertNotMatching_MatchedOnly() throws InterruptedException {

    // CountDown lock
    final CountDownLatch lock = new CountDownLatch(1);

    ObservableReader.ReaderObserver obs =
        new ObservableReader.ReaderObserver() {
          @Override
          public void update(ReaderEvent event) {
            // no event should be thrown
            Assert.fail();
            lock.countDown(); // should not be called
          }
        };

    // register observer
    virtualReader.addObserver(obs);

    String poAid = "A000000291A000000192"; // not matching poAid

    CardSelectionsService cardSelectionsService = new CardSelectionsService();

    GenericCardSelectionRequest genericCardSelectionRequest =
        new GenericCardSelectionRequest(
            CardSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poAid).build())
                .build());

    cardSelectionsService.prepareSelection(genericCardSelectionRequest);

    ((ObservableReader) virtualReader)
        .setDefaultSelectionRequest(
            cardSelectionsService.getDefaultSelectionsRequest(),
            ObservableReader.NotificationMode.MATCHED_ONLY);

    // wait 1 second
    logger.debug("Wait 1 second before inserting card");
    Thread.sleep(500);

    // test
    nativeReader.insertSe(hoplinkSE());

    Thread.sleep(100);
    nativeReader.removeSe();

    // lock thread for 2 seconds max to wait for the event
    lock.await(3, TimeUnit.SECONDS);

    // remove observer
    virtualReader.removeObserver(obs);

    Assert.assertEquals(1, lock.getCount()); // should be 1 because countDown is never called
  }

  @Test
  public void testInsertNotMatching_Always() throws InterruptedException {

    // CountDown lock
    final CountDownLatch lock = new CountDownLatch(1);

    // register observer
    ObservableReader.ReaderObserver obs =
        new ObservableReader.ReaderObserver() {
          @Override
          public void update(ReaderEvent event) {
            Assert.assertEquals(event.getReaderName(), virtualReader.getName());
            Assert.assertEquals(event.getPluginName(), masterAPI.getPlugin().getName());

            // an CARD_INSERTED event is thrown
            Assert.assertEquals(ReaderEvent.EventType.CARD_INSERTED, event.getEventType());

            // card has not match
            Assert.assertFalse(
                ((DefaultSelectionsResponse) event.getDefaultSelectionsResponse())
                    .getSelectionCardResponses()
                    .get(0)
                    .getSelectionStatus()
                    .hasMatched());

            lock.countDown(); // should be called
          }
        };

    virtualReader.addObserver(obs);

    String poAid = "A000000291A000000192"; // not matching poAid

    CardSelectionsService cardSelectionsService = new CardSelectionsService();

    GenericCardSelectionRequest genericCardSelectionRequest =
        new GenericCardSelectionRequest(
            CardSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poAid).build())
                .build());

    cardSelectionsService.prepareSelection(genericCardSelectionRequest);

    ((ObservableReader) virtualReader)
        .setDefaultSelectionRequest(
            cardSelectionsService.getDefaultSelectionsRequest(),
            ObservableReader.NotificationMode.ALWAYS);

    // wait 1 second
    logger.debug("Wait 1 second before inserting card");
    Thread.sleep(500);

    nativeReader.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);

    // test
    nativeReader.insertSe(StubReaderTest.hoplinkSE());

    // lock thread for 2 seconds max to wait for the event
    lock.await(5, TimeUnit.SECONDS);

    nativeReader.stopCardDetection();

    // remove observer
    virtualReader.removeObserver(obs);

    Assert.assertEquals(0, lock.getCount()); // should be 0 because countDown is called by
    // observer

  }

  @Test
  public void processExplicitSelectionByAtr() throws InterruptedException, KeypleReaderException {

    // Insert a card
    nativeReader.insertSe(hoplinkSE());

    logger.info("Prepare card selection");
    CardSelectionsService cardSelectionsService = new CardSelectionsService();
    GenericCardSelectionRequest genericCardSelectionRequest =
        new GenericCardSelectionRequest(
            CardSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .atrFilter(new CardSelector.AtrFilter("3B.*"))
                .build());

    /* Prepare selector, ignore AbstractSmartCard here */
    cardSelectionsService.prepareSelection(genericCardSelectionRequest);

    logger.info("Process explicit card selection");

    CardSelectionsResult cardSelectionsResult = null;
    try {
      cardSelectionsResult = cardSelectionsService.processExplicitSelections(virtualReader);
    } catch (KeypleException e) {
      Assert.fail("Unexpected exception");
    }

    logger.info("Explicit card selection result : {}", cardSelectionsResult);

    AbstractSmartCard smartCard = cardSelectionsResult.getActiveSmartCard();

    nativeReader.removeSe();

    Assert.assertNotNull(smartCard);
  }

  @Test
  public void processExplicitSelection_onEvent() throws InterruptedException {

    // CountDown lock
    final CountDownLatch lock = new CountDownLatch(1);

    ObservableReader.ReaderObserver virtualReaderObs =
        new ObservableReader.ReaderObserver() {
          @Override
          public void update(final ReaderEvent event) {

            Assert.assertEquals(ReaderEvent.EventType.CARD_INSERTED, event.getEventType());
            logger.info("Prepare card selection");
            CardSelectionsService cardSelectionsService = new CardSelectionsService();
            GenericCardSelectionRequest genericCardSelectionRequest =
                new GenericCardSelectionRequest(
                    CardSelector.builder()
                        .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                        .atrFilter(new CardSelector.AtrFilter("3B.*"))
                        .build());

            /* Prepare selector, ignore AbstractSmartCard here */
            cardSelectionsService.prepareSelection(genericCardSelectionRequest);

            logger.info("Process explicit card selection");

            CardSelectionsResult cardSelectionsResult = null;
            try {
              cardSelectionsResult = cardSelectionsService.processExplicitSelections(virtualReader);

            } catch (KeypleReaderException e) {
              Assert.fail("Unexpected exception");
            } catch (KeypleException e) {
              Assert.fail("Unexpected exception");
            }

            logger.info("Explicit card selection result : {}", cardSelectionsResult);

            AbstractSmartCard smartCard = cardSelectionsResult.getActiveSmartCard();

            Assert.assertNotNull(smartCard);

            // unlock thread
            lock.countDown();
          }
        };

    // register observer
    virtualReader.addObserver(virtualReaderObs);

    nativeReader.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);

    // test
    logger.info("Inserting card");
    nativeReader.insertSe(hoplinkSE());
    // Thread.sleep(2000);
    // nativeReader.removeSe();

    // lock thread for 5 seconds max to wait for the event
    logger.info("Lock main thread, wait for event to release this thread");
    lock.await(5, TimeUnit.SECONDS);

    nativeReader.stopCardDetection();

    Assert.assertEquals(0, lock.getCount()); // should be 0 because countDown is called by

    // remove observer
    virtualReader.removeObserver(virtualReaderObs);
  }

  /*
   * HeLPERS
   */

  /** Create a new class extending AbstractCardSelection */
  private class GenericCardSelectionRequest extends AbstractCardSelection {
    public GenericCardSelectionRequest(CardSelector cardSelector) {
      super(cardSelector);
    }

    @Override
    protected AbstractSmartCard parse(CardResponse cardResponse) {
      class GenericSmartCard extends AbstractSmartCard {
        public GenericSmartCard(CardResponse cardSelectionResponse) {
          super(cardSelectionResponse);
        }
      }
      return new GenericSmartCard(cardResponse);
    }
  }
}
