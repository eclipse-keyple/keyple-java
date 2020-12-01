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
package org.eclipse.keyple.plugin.stub;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.builder.IncreaseCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.transaction.PoSelection;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.core.card.message.ApduRequest;
import org.eclipse.keyple.core.card.message.CardRequest;
import org.eclipse.keyple.core.card.message.CardResponse;
import org.eclipse.keyple.core.card.message.CardSelectionRequest;
import org.eclipse.keyple.core.card.message.CardSelectionResponse;
import org.eclipse.keyple.core.card.message.ChannelControl;
import org.eclipse.keyple.core.card.message.DefaultSelectionsResponse;
import org.eclipse.keyple.core.card.message.ProxyReader;
import org.eclipse.keyple.core.card.selection.AbstractCardSelection;
import org.eclipse.keyple.core.card.selection.AbstractSmartCard;
import org.eclipse.keyple.core.card.selection.CardSelectionsResult;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StubReaderTest extends BaseStubTest {

  // TODO Add tests to check the CONTINUE and STOP behaviors
  // TODO Add tests to check the setThreadTimeout method and its effect

  Logger logger = LoggerFactory.getLogger(StubReaderTest.class);

  ObservableReader.ReaderObserver readerObs;

  // init before each test
  @Before
  public void SetUp() throws Exception {

    this.registerStub();
  }

  @After
  public void tearDown() {
    final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
    stubPlugin.clearObservers();
    reader.removeObserver(readerObs);
    readerObs = null;
    Assert.assertEquals(0, ((ObservableReader) reader).countObservers());
    stubPlugin.unplugReader("StubReaderTest", true);
  }

  /*
   * TEST
   *
   * EVENT
   *
   */

  /**
   * Insert card check : event and card presence
   *
   * @throws InterruptedException
   */
  @Test
  public void testInsert() throws Exception {

    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());

    final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
    Assert.assertEquals(false, reader.isCardPresent());

    // CountDown lock
    final CountDownLatch lock = new CountDownLatch(1);

    readerObs =
        new ObservableReader.ReaderObserver() {
          @Override
          public void update(ReaderEvent event) {
            if (event.getEventType() == ReaderEvent.EventType.CARD_INSERTED) {
              Assert.assertEquals(event.getReaderName(), reader.getName());
              Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
              Assert.assertEquals(ReaderEvent.EventType.CARD_INSERTED, event.getEventType());

              logger.debug("testInsert event is correct");

              lock.countDown();
            }
          }
        };

    // add observer
    reader.addObserver(readerObs);

    reader.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);

    // test
    reader.insertCard(hoplinkSE());

    // lock thread for 2 seconds max to wait for the event
    lock.await(2, TimeUnit.SECONDS);

    reader.stopCardDetection();

    reader.removeObserver(readerObs);

    Assert.assertEquals(0, lock.getCount()); // should be 0 because countDown is called by
    Assert.assertTrue(reader.isCardPresent());
  }

  /**
   * Remove card check : event and card presence
   *
   * @throws InterruptedException
   */
  @Test
  public void testRemove() throws Exception {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    // CountDown lock
    final CountDownLatch insertLock = new CountDownLatch(1);
    final CountDownLatch removeLock = new CountDownLatch(1);

    // add observer
    readerObs =
        new ObservableReader.ReaderObserver() {
          int event_i = 1;

          @Override
          public void update(ReaderEvent event) {
            logger.info("event {}", event.getEventType());
            // first event
            if (event_i == 1) {
              Assert.assertEquals(event.getReaderName(), reader.getName());
              Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
              Assert.assertEquals(ReaderEvent.EventType.CARD_INSERTED, event.getEventType());
              insertLock.countDown();
            }

            // analyze the second event, should be a CARD_REMOVED
            if (event_i == 2) {
              Assert.assertEquals(event.getReaderName(), reader.getName());
              Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
              Assert.assertEquals(ReaderEvent.EventType.CARD_REMOVED, event.getEventType());
              removeLock.countDown();
            }
            event_i++;
          }
        };

    // add observer
    reader.addObserver(readerObs);

    reader.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);

    // test
    reader.insertCard(hoplinkSE());

    // lock thread for 2 seconds max to wait for the event CARD_INSERTED
    insertLock.await(2, TimeUnit.SECONDS);

    Assert.assertEquals(0, insertLock.getCount()); // should be 0 because insertLock is
    // countDown by obs

    ((ProxyReader) reader).releaseChannel();
    reader.removeCard();

    // lock thread for 2 seconds max to wait for the event CARD_REMOVED
    removeLock.await(2, TimeUnit.SECONDS);

    Assert.assertEquals(0, removeLock.getCount()); // should be 0 because removeLock is
    // countDown by obs

    Assert.assertFalse(reader.isCardPresent());
  }

  /**
   * Remove card check : event and card presence
   *
   * @throws InterruptedException
   */
  @Test
  public void A_testInsertRemoveTwice() throws Exception {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    // CountDown lock
    final CountDownLatch firstInsertLock = new CountDownLatch(1);
    final CountDownLatch firstRemoveLock = new CountDownLatch(1);
    final CountDownLatch secondInsertLock = new CountDownLatch(1);
    final CountDownLatch secondRemoveLock = new CountDownLatch(1);

    // add observer
    readerObs =
        new ObservableReader.ReaderObserver() {
          int event_i = 1;

          @Override
          public void update(ReaderEvent event) {
            logger.info("event {}", event.getEventType());
            // first event
            if (event_i == 1) {
              Assert.assertEquals(event.getReaderName(), reader.getName());
              Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
              Assert.assertEquals(ReaderEvent.EventType.CARD_INSERTED, event.getEventType());
              firstInsertLock.countDown();
            }

            // analyze the second event, should be a CARD_REMOVED
            if (event_i == 2) {
              Assert.assertEquals(event.getReaderName(), reader.getName());
              Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
              Assert.assertEquals(ReaderEvent.EventType.CARD_REMOVED, event.getEventType());
              firstRemoveLock.countDown();
            }
            if (event_i == 3) {
              Assert.assertEquals(event.getReaderName(), reader.getName());
              Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
              Assert.assertEquals(ReaderEvent.EventType.CARD_INSERTED, event.getEventType());
              secondInsertLock.countDown();
            }
            if (event_i == 4) {
              Assert.assertEquals(event.getReaderName(), reader.getName());
              Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
              Assert.assertEquals(ReaderEvent.EventType.CARD_REMOVED, event.getEventType());
              secondRemoveLock.countDown();
            }
            event_i++;
          }
        };

    // add observer
    reader.addObserver(readerObs);

    // set PollingMode to Continue
    reader.startCardDetection(ObservableReader.PollingMode.REPEATING);

    // test first sequence
    reader.insertCard(hoplinkSE());

    // Thread.sleep(200);

    // lock thread for 2 seconds max to wait for the event CARD_INSERTED
    firstInsertLock.await(2, TimeUnit.SECONDS);
    Assert.assertEquals(0, firstInsertLock.getCount()); // should be 0 because insertLock is
    // countDown by obs
    // Thread.sleep(1000);

    ((ProxyReader) reader).releaseChannel();
    reader.removeCard();

    // lock thread for 2 seconds max to wait for the event CARD_REMOVED
    firstRemoveLock.await(2, TimeUnit.SECONDS);
    Assert.assertEquals(0, firstRemoveLock.getCount()); // should be 0 because removeLock is
    // countDown by obs

    // BUG, insert event is not throw without (1)
    // BUG (1) make thread sleep
    // BUG, solved by setting a lower threadWaitTimeout (100ms)
    // Thread.sleep(1000);

    // test second sequence
    reader.insertCard(hoplinkSE());

    // lock thread for 2 seconds max to wait for the event CARD_INSERTED
    secondInsertLock.await(2, TimeUnit.SECONDS);

    Assert.assertEquals(0, secondInsertLock.getCount()); // should be 0 because insertLock is
    // countDown by obs

    ((ProxyReader) reader).releaseChannel();

    // Thread.sleep(1000);
    reader.stopCardDetection();

    reader.removeCard();

    // lock thread for 2 seconds max to wait for the event CARD_REMOVED
    secondRemoveLock.await(2, TimeUnit.SECONDS);
    Assert.assertEquals(0, secondRemoveLock.getCount()); // should be 0 because removeLock is
    // countDown by obs

    Assert.assertFalse(reader.isCardPresent());
  }

  @Test
  public void A_testInsertRemoveTwiceFast() throws Exception {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    // CountDown lock
    final CountDownLatch firstInsertLock = new CountDownLatch(1);
    final CountDownLatch firstRemoveLock = new CountDownLatch(1);
    final CountDownLatch secondInsertLock = new CountDownLatch(1);
    final CountDownLatch secondRemoveLock = new CountDownLatch(1);

    // add observer
    readerObs =
        new ObservableReader.ReaderObserver() {
          int event_i = 1;

          @Override
          public void update(ReaderEvent event) {
            logger.info("event {}", event.getEventType());
            // first event
            if (event_i == 1) {
              Assert.assertEquals(event.getReaderName(), reader.getName());
              Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
              Assert.assertEquals(ReaderEvent.EventType.CARD_INSERTED, event.getEventType());
              firstInsertLock.countDown();
            }

            // analyze the second event, should be a CARD_REMOVED
            if (event_i == 2) {
              Assert.assertEquals(event.getReaderName(), reader.getName());
              Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
              Assert.assertEquals(ReaderEvent.EventType.CARD_REMOVED, event.getEventType());
              firstRemoveLock.countDown();
            }
            if (event_i == 3) {
              Assert.assertEquals(event.getReaderName(), reader.getName());
              Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
              Assert.assertEquals(ReaderEvent.EventType.CARD_INSERTED, event.getEventType());
              secondInsertLock.countDown();
            }
            if (event_i == 4) {
              Assert.assertEquals(event.getReaderName(), reader.getName());
              Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
              Assert.assertEquals(ReaderEvent.EventType.CARD_REMOVED, event.getEventType());
              secondRemoveLock.countDown();
            }
            event_i++;
          }
        };

    // add observer
    reader.addObserver(readerObs);

    // set PollingMode to Continue
    reader.startCardDetection(ObservableReader.PollingMode.REPEATING);

    // test first sequence
    reader.insertCard(hoplinkSE());

    // lock thread for 2 seconds max to wait for the event CARD_INSERTED
    firstInsertLock.await(2, TimeUnit.SECONDS);
    Assert.assertEquals(0, firstInsertLock.getCount()); // should be 0 because insertLock is
    // countDown by obs

    ((ProxyReader) reader).releaseChannel();
    reader.removeCard();

    // lock thread for 2 seconds max to wait for the event CARD_REMOVED
    firstRemoveLock.await(2, TimeUnit.SECONDS);
    Assert.assertEquals(0, firstRemoveLock.getCount()); // should be 0 because removeLock is
    // countDown by obs

    // BUG, insert event is not throw without (1)
    // BUG (1) make thread sleep
    // BUG, solved by setting a lower threadWaitTimeout (100ms)

    // test second sequence
    reader.insertCard(hoplinkSE());

    // lock thread for 2 seconds max to wait for the event CARD_INSERTED
    secondInsertLock.await(2, TimeUnit.SECONDS);

    reader.stopCardDetection();

    Assert.assertEquals(0, secondInsertLock.getCount()); // should be 0 because insertLock is
    // countDown by obs
    ((ProxyReader) reader).releaseChannel();
    reader.removeCard();

    // lock thread for 2 seconds max to wait for the event CARD_REMOVED
    secondRemoveLock.await(2, TimeUnit.SECONDS);
    Assert.assertEquals(0, secondRemoveLock.getCount()); // should be 0 because removeLock is
    // countDown by obs

    Assert.assertFalse(reader.isCardPresent());
  }

  @Test
  public void testInsertSmartCard() throws Exception {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    // CountDown lock
    final CountDownLatch lock = new CountDownLatch(1);
    final String poAid = "A000000291A000000191";

    // activate ISO_14443_4
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // add observer
    readerObs =
        new ObservableReader.ReaderObserver() {
          @Override
          public void update(ReaderEvent event) {
            if (event.getEventType() == ReaderEvent.EventType.CARD_MATCHED) {
              Assert.assertEquals(event.getReaderName(), reader.getName());
              Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
              Assert.assertEquals(ReaderEvent.EventType.CARD_MATCHED, event.getEventType());
              Assert.assertTrue(
                  ((DefaultSelectionsResponse) event.getDefaultSelectionsResponse())
                      .getCardSelectionResponses()
                      .get(0)
                      .getSelectionStatus()
                      .hasMatched());
              Assert.assertArrayEquals(
                  ((DefaultSelectionsResponse) event.getDefaultSelectionsResponse())
                      .getCardSelectionResponses()
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
                      .getCardSelectionResponses()
                      .get(0)
                      .getSelectionStatus()
                      .getFci()
                      .getBytes(),
                  fci);

              logger.debug("match event is correct");
              // unlock thread
              lock.countDown();
            }
          }
        };

    // add observer
    reader.addObserver(readerObs);

    CardSelectionsService cardSelectionsService = new CardSelectionsService();

    PoSelection poSelection =
        new PoSelection(
            PoSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poAid).build())
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                .build());

    cardSelectionsService.prepareSelection(poSelection);

    ((ObservableReader) reader)
        .setDefaultSelectionRequest(
            cardSelectionsService.getDefaultSelectionsRequest(),
            ObservableReader.NotificationMode.MATCHED_ONLY);

    // set PollingMode to Continue
    reader.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);

    // test
    reader.insertCard(hoplinkSE());

    lock.await(1, TimeUnit.SECONDS);

    Assert.assertEquals(0, lock.getCount()); // should be 0 because countDown is called by
  }

  @Test
  public void testInsertNotMatching_MatchedOnly() throws Exception {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    // CountDown lock
    final CountDownLatch lock = new CountDownLatch(1);

    // add observer
    readerObs =
        new ObservableReader.ReaderObserver() {
          @Override
          public void update(ReaderEvent event) {
            // only CARD_REMOVED event should be thrown
            if (event.getEventType() != ReaderEvent.EventType.CARD_REMOVED) {
              lock.countDown(); // should not be called
            }
          }
        };

    // activate ISO_14443_4
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // add observer
    reader.addObserver(readerObs);

    String poAid = "A000000291A000000192"; // not matching poAid

    CardSelectionsService cardSelectionsService = new CardSelectionsService();

    PoSelection poSelection =
        new PoSelection(
            PoSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poAid).build())
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                .build());

    cardSelectionsService.prepareSelection(poSelection);

    ((ObservableReader) reader)
        .setDefaultSelectionRequest(
            cardSelectionsService.getDefaultSelectionsRequest(),
            ObservableReader.NotificationMode.MATCHED_ONLY);

    reader.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);

    // test
    reader.insertCard(hoplinkSE());

    Thread.sleep(100);
    reader.removeCard();

    reader.stopCardDetection();

    // lock thread for 2 seconds max to wait for the event
    lock.await(100, TimeUnit.MILLISECONDS);

    Assert.assertEquals(1, lock.getCount()); // should be 1 because countDown is never called
  }

  @Test
  public void testInsertNotMatching_Always() throws Exception {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    // CountDown lock
    final CountDownLatch lock = new CountDownLatch(1);

    // activate ISO_14443_4
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // add observer
    readerObs =
        new ObservableReader.ReaderObserver() {
          @Override
          public void update(ReaderEvent event) {
            // Only CARD_INSERTED/CARD_REMOVED event are thrown
            if (event.getEventType() == ReaderEvent.EventType.CARD_INSERTED) {
              Assert.assertEquals(event.getReaderName(), reader.getName());
              Assert.assertEquals(event.getPluginName(), stubPlugin.getName());

              // card has not match
              Assert.assertFalse(
                  ((DefaultSelectionsResponse) event.getDefaultSelectionsResponse())
                      .getCardSelectionResponses()
                      .get(0)
                      .getSelectionStatus()
                      .hasMatched());

              lock.countDown(); // should be called
            }
          }
        };

    // add observer
    reader.addObserver(readerObs);

    String poAid = "A000000291A000000192"; // not matching poAid

    CardSelectionsService cardSelectionsService = new CardSelectionsService();

    PoSelection poSelection =
        new PoSelection(
            PoSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poAid).build())
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                .build());

    cardSelectionsService.prepareSelection(poSelection);

    ((ObservableReader) reader)
        .setDefaultSelectionRequest(
            cardSelectionsService.getDefaultSelectionsRequest(),
            ObservableReader.NotificationMode.ALWAYS);

    reader.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);

    // test
    reader.insertCard(hoplinkSE());

    // lock thread for 2 seconds max to wait for the event
    lock.await(2, TimeUnit.SECONDS);

    reader.stopCardDetection();

    Assert.assertEquals(0, lock.getCount()); // should be 0 because countDown is called by
    // observer
    reader.removeObserver(readerObs);
  }

  @Test
  public void testExplicitSelection_onEvent() throws Exception {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    // activate INNOVATRON_B_PRIME_CARD
    reader.activateProtocol(
        StubSupportedProtocols.INNOVATRON_B_PRIME_CARD.name(),
        ContactlessCardCommonProtocols.INNOVATRON_B_PRIME_CARD.name());

    // CountDown lock
    final CountDownLatch lock = new CountDownLatch(1);

    // add observer
    readerObs =
        new ObservableReader.ReaderObserver() {
          @Override
          public void update(ReaderEvent event) {
            CardSelectionsService cardSelectionsService = new CardSelectionsService();

            PoSelection poSelection =
                new PoSelection(
                    PoSelector.builder()
                        .cardProtocol(ContactlessCardCommonProtocols.INNOVATRON_B_PRIME_CARD.name())
                        .atrFilter(new PoSelector.AtrFilter("3B.*"))
                        .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                        .build());

            /* Prepare selector, ignore AbstractSmartCard here */
            cardSelectionsService.prepareSelection(poSelection);

            try {
              CardSelectionsResult cardSelectionsResult =
                  cardSelectionsService.processExplicitSelections(reader);

              AbstractSmartCard smartCard = cardSelectionsResult.getActiveSmartCard();

              Assert.assertNotNull(smartCard);

            } catch (KeypleReaderException e) {
              Assert.fail("Unexcepted exception");
            } catch (KeypleException e) {
              Assert.fail("Unexcepted exception");
            }
            // unlock thread
            lock.countDown();
          }
        };

    // add observer
    reader.addObserver(readerObs);

    reader.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);

    // test
    reader.insertCard(revision1SE());

    // lock thread for 2 seconds max to wait for the event
    lock.await(2, TimeUnit.SECONDS);

    Assert.assertEquals(0, lock.getCount()); // should be 0 because countDown is called by

    reader.stopCardDetection();
    // observer
    reader.removeObserver(readerObs);
  }

  @Test
  public void testReleaseSeChannel() throws InterruptedException {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    reader.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);

    // CountDown lock
    final CountDownLatch lock = new CountDownLatch(1);
    final String poAid = "A000000291A000000191";

    // activate ISO_14443_4
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // add observer
    readerObs =
        new ObservableReader.ReaderObserver() {
          @Override
          public void update(ReaderEvent event) {

            if (ReaderEvent.EventType.CARD_MATCHED == event.getEventType()) {
              logger.info("CARD_MATCHED event received");
              logger.info("Notify card processed after 0ms");
              ((ProxyReader) reader).releaseChannel();
              lock.countDown();
            }
          }
        };

    // add observer
    reader.addObserver(readerObs);

    CardSelectionsService cardSelectionsService = new CardSelectionsService();

    PoSelection poSelection =
        new PoSelection(
            PoSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poAid).build())
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                .build());

    cardSelectionsService.prepareSelection(poSelection);

    reader.setDefaultSelectionRequest(
        cardSelectionsService.getDefaultSelectionsRequest(),
        ObservableReader.NotificationMode.MATCHED_ONLY);

    reader.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);

    // test
    reader.insertCard(hoplinkSE());

    // lock thread for 2 seconds max to wait for the event
    lock.await(2, TimeUnit.SECONDS);

    reader.stopCardDetection();

    Assert.assertEquals(0, lock.getCount()); // should be 0 because countDown is called by
    // observer

  }

  /*
   * TEST
   *
   * TRANSMIT
   *
   */

  @Test
  public void transmit_Hoplink_Successful() {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    // init Request
    List<CardSelectionRequest> cardSelectionRequests = getRequestIsoDepSetSample();

    // init card
    reader.insertCard(hoplinkSE());

    // activate ISO_14443_4
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // send the selection request
    genericSelectSe(reader);

    // test
    List<CardSelectionResponse> cardSelectionResponses =
        ((ProxyReader) reader)
            .transmitCardSelectionRequests(
                cardSelectionRequests,
                MultiSelectionProcessing.FIRST_MATCH,
                ChannelControl.KEEP_OPEN);

    // assert
    Assert.assertTrue(
        cardSelectionResponses.get(0).getCardResponse().getApduResponses().get(0).isSuccessful());
  }

  @Test(expected = KeypleReaderException.class)
  public void transmit_no_response() throws Exception {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    // init Request
    List<CardSelectionRequest> cardSelectionRequests = getNoResponseRequest();

    // init card
    reader.insertCard(noApduResponseSE());

    // activate ISO_14443_4
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // send the selection request
    genericSelectSe(reader);

    // test
    List<CardSelectionResponse> cardSelectionResponses =
        ((ProxyReader) reader)
            .transmitCardSelectionRequests(
                cardSelectionRequests,
                MultiSelectionProcessing.FIRST_MATCH,
                ChannelControl.KEEP_OPEN);
  }

  @Test
  public void transmit_partial_response_set_0() {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    // init Request
    List<CardSelectionRequest> cardSelectionRequests = getPartialRequestList(0);

    // init card
    reader.insertCard(partialSE());

    // activate ISO_14443_4
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // send the selection request
    genericSelectSe(reader);

    // test
    try {
      List<CardSelectionResponse> cardSelectionResponses =
          ((ProxyReader) reader)
              .transmitCardSelectionRequests(
                  cardSelectionRequests,
                  MultiSelectionProcessing.FIRST_MATCH,
                  ChannelControl.KEEP_OPEN);
      Assert.fail("Should throw exception");

    } catch (KeypleReaderIOException ex) {
      Assert.assertEquals(1, ex.getCardSelectionResponses().size());
      Assert.assertEquals(
          2, ex.getCardSelectionResponses().get(0).getCardResponse().getApduResponses().size());
    }
  }

  // @Test
  public void transmit_partial_response_set_1() {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
    // init Request
    List<CardSelectionRequest> cardSelectionRequests = getPartialRequestList(1);

    // init card
    reader.insertCard(partialSE());

    // activate ISO_14443_4
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // send the selection request
    genericSelectSe(reader);

    // test
    try {
      List<CardSelectionResponse> cardSelectionResponses =
          ((ProxyReader) reader)
              .transmitCardSelectionRequests(
                  cardSelectionRequests,
                  MultiSelectionProcessing.FIRST_MATCH,
                  ChannelControl.KEEP_OPEN);
      Assert.fail("Should throw exception");

    } catch (KeypleReaderIOException ex) {
      Assert.assertEquals(2, ex.getCardSelectionResponses().size());
      Assert.assertEquals(
          4, ex.getCardSelectionResponses().get(0).getCardResponse().getApduResponses().size());
      Assert.assertEquals(
          2, ex.getCardSelectionResponses().get(1).getCardResponse().getApduResponses().size());
      Assert.assertEquals(
          2, ex.getCardSelectionResponses().get(1).getCardResponse().getApduResponses().size());
    }
  }

  // @Test
  public void transmit_partial_response_set_2() {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
    // init Request
    List<CardSelectionRequest> cardSelectionRequests = getPartialRequestList(2);

    // init card
    reader.insertCard(partialSE());

    // activate ISO_14443_4
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // send the selection request
    genericSelectSe(reader);

    // test
    try {
      List<CardSelectionResponse> cardSelectionResponses =
          ((ProxyReader) reader)
              .transmitCardSelectionRequests(
                  cardSelectionRequests,
                  MultiSelectionProcessing.FIRST_MATCH,
                  ChannelControl.KEEP_OPEN);
      Assert.fail("Should throw exception");

    } catch (KeypleReaderIOException ex) {
      Assert.assertEquals(3, ex.getCardSelectionResponses().size());
      Assert.assertEquals(
          4, ex.getCardSelectionResponses().get(0).getCardResponse().getApduResponses().size());
      Assert.assertEquals(
          4, ex.getCardSelectionResponses().get(1).getCardResponse().getApduResponses().size());
      Assert.assertEquals(
          2, ex.getCardSelectionResponses().get(2).getCardResponse().getApduResponses().size());
    }
  }

  // @Test
  public void transmit_partial_response_set_3() {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
    // init Request
    List<CardSelectionRequest> cardSelectionRequests = getPartialRequestList(3);

    // init card
    reader.insertCard(partialSE());

    // activate ISO_14443_4
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // send the selection request
    genericSelectSe(reader);

    // test
    try {
      List<CardSelectionResponse> cardSelectionResponses =
          ((ProxyReader) reader)
              .transmitCardSelectionRequests(
                  cardSelectionRequests,
                  MultiSelectionProcessing.FIRST_MATCH,
                  ChannelControl.KEEP_OPEN);
      Assert.assertEquals(3, cardSelectionResponses.size());
      Assert.assertEquals(
          4, cardSelectionResponses.get(0).getCardResponse().getApduResponses().size());
      Assert.assertEquals(
          4, cardSelectionResponses.get(1).getCardResponse().getApduResponses().size());
      Assert.assertEquals(
          4, cardSelectionResponses.get(2).getCardResponse().getApduResponses().size());
    } catch (KeypleReaderException ex) {
      Assert.fail("Should not throw exception");
    }
  }

  @Test
  public void transmit_partial_response_0() {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    // init Request
    CardRequest cardRequest = getPartialRequest(0);

    // init card
    reader.insertCard(partialSE());

    // activate ISO_14443_4
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // send the selection request
    genericSelectSe(reader);

    // test
    try {
      CardResponse cardResponse =
          ((ProxyReader) reader).transmitCardRequest(cardRequest, ChannelControl.KEEP_OPEN);
      Assert.fail("Should throw exception");

    } catch (KeypleReaderIOException ex) {
      Assert.assertEquals(0, ex.getCardResponse().getApduResponses().size());
    }
  }

  @Test
  public void transmit_partial_response_1() {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    // init Request
    CardRequest cardRequest = getPartialRequest(1);

    // init card
    reader.insertCard(partialSE());

    // activate ISO_14443_4
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // send the selection request
    genericSelectSe(reader);

    // test
    try {
      CardResponse cardResponse =
          ((ProxyReader) reader).transmitCardRequest(cardRequest, ChannelControl.KEEP_OPEN);
      Assert.fail("Should throw exception");

    } catch (KeypleReaderIOException ex) {
      Assert.assertEquals(1, ex.getCardResponse().getApduResponses().size());
    }
  }

  @Test
  public void transmit_partial_response_2() {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    // init Request
    CardRequest cardRequest = getPartialRequest(2);

    // init card
    reader.insertCard(partialSE());

    // activate ISO_14443_4
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // send the selection request
    genericSelectSe(reader);

    // test
    try {
      CardResponse cardResponse =
          ((ProxyReader) reader).transmitCardRequest(cardRequest, ChannelControl.KEEP_OPEN);
      Assert.fail("Should throw exception");

    } catch (KeypleReaderIOException ex) {
      Assert.assertEquals(2, ex.getCardResponse().getApduResponses().size());
    }
  }

  @Test
  public void transmit_partial_response_3() {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

    // init Request
    CardRequest cardRequest = getPartialRequest(3);

    // init card
    reader.insertCard(partialSE());

    // activate ISO_14443_4
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // send the selection request
    genericSelectSe(reader);

    // test
    try {
      CardResponse cardResponse =
          ((ProxyReader) reader).transmitCardRequest(cardRequest, ChannelControl.KEEP_OPEN);
      Assert.assertEquals(3, cardResponse.getApduResponses().size());
    } catch (KeypleReaderException ex) {
      Assert.fail("Should not throw exception");
    }
  }

  /*
   * NAME and PARAMETERS
   */

  @Test
  public void testGetName() {
    stubPlugin.plugReader("StubReaderTest", true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
    Assert.assertNotNull(reader.getName());
  }

  /*
   * HELPER METHODS
   */

  public static List<CardSelectionRequest> getRequestIsoDepSetSample() {
    String poAid = "A000000291A000000191";
    ReadRecordsCmdBuild poReadRecordCmd_T2Env =
        new ReadRecordsCmdBuild(PoClass.ISO, 0x14, 1, ReadRecordsCmdBuild.ReadMode.ONE_RECORD, 32);

    List<ApduRequest> poApduRequests = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());

    CardSelector cardSelector =
        CardSelector.builder()
            .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poAid).build())
            .build();
    CardSelectionRequest cardSelectionRequest =
        new CardSelectionRequest(cardSelector, new CardRequest(poApduRequests));

    List<CardSelectionRequest> cardSelectionRequests = new ArrayList<CardSelectionRequest>();

    cardSelectionRequests.add(cardSelectionRequest);

    return cardSelectionRequests;
  }

  /*
   * No Response: increase command is not defined in the StubSE
   *
   * An Exception will be thrown.
   */
  public static List<CardSelectionRequest> getNoResponseRequest() {
    String poAid = "A000000291A000000191";
    IncreaseCmdBuild poIncreaseCmdBuild =
        new IncreaseCmdBuild(PoClass.ISO, (byte) 0x14, (byte) 0x01, 0);

    List<ApduRequest> poApduRequests = Arrays.asList(poIncreaseCmdBuild.getApduRequest());

    CardSelector cardSelector =
        CardSelector.builder()
            .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poAid).build())
            .build();
    CardSelectionRequest cardSelectionRequest =
        new CardSelectionRequest(cardSelector, new CardRequest(poApduRequests));

    List<CardSelectionRequest> cardSelectionRequests = new ArrayList<CardSelectionRequest>();

    cardSelectionRequests.add(cardSelectionRequest);

    return cardSelectionRequests;
  }

  /*
   * Partial response set: multiple read records commands, one is not defined in the StubSE
   *
   * An Exception will be thrown.
   */
  public static List<CardSelectionRequest> getPartialRequestList(int scenario) {
    String poAid = "A000000291A000000191";
    ReadRecordsCmdBuild poReadRecord1CmdBuild =
        new ReadRecordsCmdBuild(PoClass.ISO, 0x14, 1, ReadRecordsCmdBuild.ReadMode.ONE_RECORD, 0);

    /* this command doesn't in the PartialSE */
    ReadRecordsCmdBuild poReadRecord2CmdBuild =
        new ReadRecordsCmdBuild(PoClass.ISO, 0x1E, 1, ReadRecordsCmdBuild.ReadMode.ONE_RECORD, 0);

    CardSelector cardSelector =
        CardSelector.builder()
            .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poAid).build())
            .build();

    List<ApduRequest> poApduRequests1 = new ArrayList<ApduRequest>();
    poApduRequests1.add(poReadRecord1CmdBuild.getApduRequest());
    poApduRequests1.add(poReadRecord1CmdBuild.getApduRequest());
    poApduRequests1.add(poReadRecord1CmdBuild.getApduRequest());
    poApduRequests1.add(poReadRecord1CmdBuild.getApduRequest());

    List<ApduRequest> poApduRequests2 = new ArrayList<ApduRequest>();
    poApduRequests2.add(poReadRecord1CmdBuild.getApduRequest());
    poApduRequests2.add(poReadRecord1CmdBuild.getApduRequest());
    poApduRequests2.add(poReadRecord1CmdBuild.getApduRequest());
    poApduRequests2.add(poReadRecord1CmdBuild.getApduRequest());

    List<ApduRequest> poApduRequests3 = new ArrayList<ApduRequest>();
    poApduRequests3.add(poReadRecord1CmdBuild.getApduRequest());
    poApduRequests3.add(poReadRecord1CmdBuild.getApduRequest());
    poApduRequests3.add(poReadRecord2CmdBuild.getApduRequest());
    poApduRequests3.add(poReadRecord1CmdBuild.getApduRequest());

    CardSelectionRequest selectionRequest1 =
        new CardSelectionRequest(cardSelector, new CardRequest(poApduRequests1));

    CardSelectionRequest selectionRequest2 =
        new CardSelectionRequest(cardSelector, new CardRequest(poApduRequests2));

    /* This CardRequest fails at step 3 */
    CardSelectionRequest selectionRequest3 =
        new CardSelectionRequest(cardSelector, new CardRequest(poApduRequests3));

    CardSelectionRequest selectionRequest4 =
        new CardSelectionRequest(cardSelector, new CardRequest(poApduRequests1));

    List<CardSelectionRequest> cardSelectionRequests = new ArrayList<CardSelectionRequest>();

    switch (scenario) {
      case 0:
        /* 0 response */
        cardSelectionRequests.add(selectionRequest3); // fails
        cardSelectionRequests.add(selectionRequest1); // succeeds
        cardSelectionRequests.add(selectionRequest2); // succeeds
        break;
      case 1:
        /* 1 response */
        cardSelectionRequests.add(selectionRequest1); // succeeds
        cardSelectionRequests.add(selectionRequest3); // fails
        cardSelectionRequests.add(selectionRequest2); // succeeds
        break;
      case 2:
        /* 2 responses */
        cardSelectionRequests.add(selectionRequest1); // succeeds
        cardSelectionRequests.add(selectionRequest2); // succeeds
        cardSelectionRequests.add(selectionRequest3); // fails
        break;
      case 3:
        /* 3 responses */
        cardSelectionRequests.add(selectionRequest1); // succeeds
        cardSelectionRequests.add(selectionRequest2); // succeeds
        cardSelectionRequests.add(selectionRequest4); // succeeds
        break;
      default:
    }

    return cardSelectionRequests;
  }

  /*
   * Partial response: multiple read records commands, one is not defined in the StubSE
   *
   * An Exception will be thrown.
   */
  public static CardRequest getPartialRequest(int scenario) {
    String poAid = "A000000291A000000191";

    ReadRecordsCmdBuild poReadRecord1CmdBuild =
        new ReadRecordsCmdBuild(PoClass.ISO, 0x14, 1, ReadRecordsCmdBuild.ReadMode.ONE_RECORD, 0);

    /* this command doesn't in the PartialSE */
    ReadRecordsCmdBuild poReadRecord2CmdBuild =
        new ReadRecordsCmdBuild(PoClass.ISO, 0x1E, 1, ReadRecordsCmdBuild.ReadMode.ONE_RECORD, 0);

    List<ApduRequest> poApduRequests = new ArrayList<ApduRequest>();

    switch (scenario) {
      case 0:
        poApduRequests.add(poReadRecord2CmdBuild.getApduRequest()); // fails
        poApduRequests.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
        poApduRequests.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
        break;
      case 1:
        poApduRequests.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
        poApduRequests.add(poReadRecord2CmdBuild.getApduRequest()); // fails
        poApduRequests.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
        break;
      case 2:
        poApduRequests.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
        poApduRequests.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
        poApduRequests.add(poReadRecord2CmdBuild.getApduRequest()); // fails
        break;
      case 3:
        poApduRequests.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
        poApduRequests.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
        poApduRequests.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
        break;
      default:
        break;
    }

    return new CardRequest(poApduRequests);
  }

  public static StubSmartCard hoplinkSE() {

    return new StubSmartCard() {

      @Override
      public byte[] processApdu(byte[] apduIn) {
        addHexCommand(
            "00 A4 04 00 0A A0 00 00 02 91 A0 00 00 01 91 00",
            "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000");

        addHexCommand("00 A4 04 00 0A A0 00 00 02 91 A0 00 00 01 92 00", "6A82");

        addHexCommand(
            "00 B2 01 A4 20",
            "00000000000000000000000000000000000000000000000000000000000000009000");

        return super.processApdu(apduIn);
      }

      @Override
      public byte[] getATR() {
        return ByteArrayUtil.fromHex("3B 8E 80 01 80 31 80 66 40 90 89 12 08 02 83 01 90 00 0B");
      }

      @Override
      public String getCardProtocol() {
        return "ISO_14443_4";
      }
    };
  }

  public static StubSmartCard revision1SE() {
    return new StubSmartCard() {
      @Override
      public byte[] processApdu(byte[] apduIn) {
        addHexCommand(
            "00 A4 04 00 0A A0 00 00 02 91 A0 00 00 01 91 00",
            "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000");

        addHexCommand("00 A4 04 00 0A A0 00 00 02 91 A0 00 00 01 92 00", "6A82");

        addHexCommand(
            "00 B2 01 A4 20",
            "00000000000000000000000000000000000000000000000000000000000000009000");

        return super.processApdu(apduIn);
      }

      @Override
      public byte[] getATR() {
        return ByteArrayUtil.fromHex("3b 8f 80 01 80 5a 08 03 04 00 02 00 21 72 90 ff 82 90 00 f3");
      }

      @Override
      public String getCardProtocol() {
        return "INNOVATRON_B_PRIME_CARD";
      }
    };
  }

  public static StubSmartCard noApduResponseSE() {
    return new StubSmartCard() {

      @Override
      public byte[] processApdu(byte[] apduIn) {

        addHexCommand(
            "00 A4 04 00 0A A0 00 00 02 91 A0 00 00 01 91 00",
            "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000");

        return super.processApdu(apduIn);
      }

      @Override
      public byte[] getATR() {
        return ByteArrayUtil.fromHex("3B 8E 80 01 80 31 80 66 40 90 89 12 08 02 83 01 90 00 0B");
      }

      @Override
      public String getCardProtocol() {
        return "ISO_14443_4";
      }
    };
  }

  public static StubSmartCard partialSE() {
    return new StubSmartCard() {
      @Override
      public byte[] processApdu(byte[] apduIn) {

        addHexCommand(
            "00 A4 04 00 0A A0 00 00 02 91 A0 00 00 01 91 00",
            "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000");
        addHexCommand(
            "00 B2 01 A4 00", "00000000000000000000000000000000000000000000000000000000009000");

        return super.processApdu(apduIn);
      }

      @Override
      public byte[] getATR() {
        return ByteArrayUtil.fromHex("3B 8E 80 01 80 31 80 66 40 90 89 12 08 02 83 01 90 00 0B");
      }

      @Override
      public String getCardProtocol() {
        return "ISO_14443_4";
      }
    };
  }

  public static StubSmartCard getSENoconnection() {
    return new StubSmartCard() {
      @Override
      public byte[] getATR() {
        return new byte[0];
      }

      @Override
      public boolean isPhysicalChannelOpen() {
        return false;
      }

      // override methods to fail open connection
      @Override
      public void openPhysicalChannel() {
        throw new KeypleReaderIOException("Impossible to establish connection");
      }

      @Override
      public void closePhysicalChannel() {
        throw new KeypleReaderIOException("Channel is not open");
      }

      @Override
      public byte[] processApdu(byte[] apduIn) {
        throw new KeypleReaderIOException("Error while transmitting apdu");
      }

      @Override
      public String getCardProtocol() {
        return "ISO_14443_4";
      }
    };
  }

  public static ApduRequest getApduSample() {
    return new ApduRequest(ByteArrayUtil.fromHex("FEDCBA98 9005h"), false);
  }

  public static void genericSelectSe(Reader reader) {
    /** Create a new local class extending AbstractCardSelection */
    class GenericCardSelectionRequest extends AbstractCardSelection {

      public GenericCardSelectionRequest(CardSelector cardSelector) {
        super(cardSelector);
      }

      @Override
      protected AbstractSmartCard parse(CardSelectionResponse cardSelectionResponse) {
        class GenericSmartCard extends AbstractSmartCard {
          public GenericSmartCard(CardSelectionResponse cardSelectionResponse) {
            super(cardSelectionResponse);
          }
        }
        return new GenericSmartCard(cardSelectionResponse);
      }
    }

    CardSelectionsService cardSelectionsService = new CardSelectionsService();
    // CardSelectionsService cardSelectionsService = new
    // CardSelectionsService(MultiSelectionProcessing.PROCESS_ALL,
    // ChannelControl.CLOSE_AFTER);
    GenericCardSelectionRequest genericCardSelectionRequest =
        new GenericCardSelectionRequest(
            CardSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .atrFilter(new CardSelector.AtrFilter("3B.*"))
                .build());

    /* Prepare selector, ignore AbstractSmartCard here */
    cardSelectionsService.prepareSelection(genericCardSelectionRequest);

    try {
      cardSelectionsService.processExplicitSelections(reader);
    } catch (KeypleException e) {
      Assert.fail("Unexpected exception");
    }
  }
}
