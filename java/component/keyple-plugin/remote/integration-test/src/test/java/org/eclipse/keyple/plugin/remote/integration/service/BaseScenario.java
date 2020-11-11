/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remote.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.*;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.SelectionsResult;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.util.NamedThreadFactory;
import org.eclipse.keyple.plugin.remote.LocalServiceClient;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointServer;
import org.eclipse.keyple.plugin.remote.impl.AbstractNode;
import org.eclipse.keyple.plugin.remote.integration.common.app.ReaderEventFilter;
import org.eclipse.keyple.plugin.remote.integration.common.app.RemotePluginObserver;
import org.eclipse.keyple.plugin.remote.integration.common.model.ConfigurationResult;
import org.eclipse.keyple.plugin.remote.integration.common.model.DeviceInput;
import org.eclipse.keyple.plugin.remote.integration.common.model.TransactionResult;
import org.eclipse.keyple.plugin.remote.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remote.integration.common.se.StubCalypsoClassic;
import org.eclipse.keyple.plugin.remote.integration.common.util.CalypsoUtilities;
import org.eclipse.keyple.plugin.remote.RemoteServiceParameters;
import org.eclipse.keyple.plugin.remote.impl.LocalServiceClientTest;
import org.eclipse.keyple.plugin.remote.RemoteServerPlugin;
import org.eclipse.keyple.plugin.remote.impl.RemoteServerPluginFactory;
import org.eclipse.keyple.plugin.remote.impl.RemoteServerUtils;
import org.eclipse.keyple.plugin.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseScenario {

  /**
   * A successful aid selection is executed locally on the terminal followed by a remoteService call
   * to launch the remote Calypso session. The Card content is sent during this first called along
   * with custom data. All this information is received by the server to select and execute the
   * corresponding ticketing scenario.
   *
   * <p>At the end of a successful calypso session, custom data is sent back to the client as a
   * final result.
   *
   * <p>This scenario can be executed on Sync node and Async node.
   */
  abstract void execute_localselection_remoteTransaction_successful();

  /**
   * The client application invokes the remoteService with enabling observability capabilities. As a
   * result the server creates a Observable Virtual Reader that receives local reader events such
   * as Card insertions and removals.
   *
   * <p>A Card Insertion is simulated locally followed by a card removal 1 second later.
   *
   * <p>The Card Insertion event is sent to the Virtual Reader whose observer starts a remote
   * Calypso session. At the end of a successful calypso session, custom data is sent back to the
   * client as a final result.
   *
   * <p>The operation is executed twice with two different users.
   *
   * <p>After the second Card insertion, Virtual Reader observers are cleared to purge the server
   * virtual reader.
   */
  abstract void observable_defaultSelection_onMatched_transaction_successful();

  /**
   * Similar to scenario 1 without the local aid selection. In this case, the server application is
   * responsible for ordering the aid selection.
   */
  abstract void execute_remoteselection_remoteTransaction_successful();

  /** Similar to scenario 3 with two concurrent clients. */
  abstract void execute_multiclient_remoteselection_remoteTransaction_successful();

  /** Client application invokes remoteService which results in a remote calypso session. */
  abstract void execute_transaction_slowSe_success();

  abstract void execute_all_methods();

  /*
   * error cases
   */

  /**
   * Client application invokes remoteService which results in a remote calypso session. Native
   * Reader throws exception in the closing operation.
   */
  abstract void execute_transaction_closeSession_card_error();

  abstract void execute_transaction_host_network_error();

  abstract void execute_transaction_client_network_error();

  private static final Logger logger = LoggerFactory.getLogger(BaseScenario.class);

  public static String NATIVE_PLUGIN_NAME = "stubPlugin";
  public static String NATIVE_READER_NAME = "stubReader";
  public static String NATIVE_READER_NAME_2 = "stubReader2";

  public static String SERVICE_ID_1 = "EXECUTE_CALYPSO_SESSION_FROM_LOCAL_SELECTION";
  public static String SERVICE_ID_2 = "CREATE_CONFIGURE_OBS_VIRTUAL_READER";
  public static String SERVICE_ID_3 = "EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION";
  public static String SERVICE_ID_4 = "EXECUTE_ALL_METHODS";

  public static String DEVICE_ID = "Xo99";

  LocalServiceClient localService;
  StubPlugin localPlugin;
  StubReader localReader;
  StubReader localReader2;

  RemoteServerPlugin remotePlugin;
  UserInput user1;
  UserInput user2;
  DeviceInput device1;

  ExecutorService clientPool = Executors.newCachedThreadPool(new NamedThreadFactory("client-pool"));
  ExecutorService serverPool = Executors.newCachedThreadPool(new NamedThreadFactory("remote-pool"));

  public static void unRegisterRemotePlugin() {
    try {
      RemoteServerPlugin oldPlugin = RemoteServerUtils.getRemotePlugin();
      SmartCardService.getInstance().unregisterPlugin(oldPlugin.getName());
    } catch (KeyplePluginNotFoundException e) {
      // no plugin to unregister
    }
  }

  /** Init local stub plugin that can work with {@link StubSecureElement} */
  void initNativeStubPlugin() {
    // reuse stub plugin
    try {
      localPlugin = (StubPlugin) SmartCardService.getInstance().getPlugin(NATIVE_PLUGIN_NAME);
    } catch (KeyplePluginNotFoundException e) {
      localPlugin =
          (StubPlugin)
              SmartCardService.getInstance()
                  .registerPlugin(new StubPluginFactory(NATIVE_PLUGIN_NAME));
    }
    // localReader should be reset
    try {
      localReader = (StubReader) localPlugin.getReader(NATIVE_READER_NAME);
    } catch (KeypleReaderNotFoundException e) {
      localPlugin.plugStubReader(NATIVE_READER_NAME, true, true);
      localReader = (StubReader) localPlugin.getReader(NATIVE_READER_NAME);
      // activate ISO_14443_4
      localReader.activateProtocol(
          StubSupportedProtocols.ISO_14443_4.name(),
          ContactlessCardCommonProtocols.ISO_14443_4.name());
    }
    // localReader should be reset
    try {
      localReader2 = (StubReader) localPlugin.getReader(NATIVE_READER_NAME_2);
    } catch (KeypleReaderNotFoundException e) {
      // plug a second reader
      localPlugin.plugStubReader(NATIVE_READER_NAME_2, true, true);
      localReader2 = (StubReader) localPlugin.getReader(NATIVE_READER_NAME_2);
      // activate ISO_14443_4
      localReader2.activateProtocol(
          StubSupportedProtocols.ISO_14443_4.name(),
          ContactlessCardCommonProtocols.ISO_14443_4.name());
    }
  }

  void clearNativeReader() {
    localPlugin.unplugStubReader(NATIVE_READER_NAME, true);
    localPlugin.unplugStubReader(NATIVE_READER_NAME_2, true);
  }

  /** Init a Sync Remote Server Plugin (ie. http server) */
  void initRemotePluginWithSyncNode() {
    try {
      remotePlugin = RemoteServerUtils.getRemotePlugin();
    } catch (KeyplePluginNotFoundException e) {
      remotePlugin =
          (RemoteServerPlugin)
              SmartCardService.getInstance()
                  .registerPlugin(
                      RemoteServerPluginFactory.builder()
                          .withSyncNode()
                          .withPluginObserver(new RemotePluginObserver())
                          // .usingDefaultEventNotificationPool()
                          .usingEventNotificationPool(serverPool)
                          .build());
    }
  }

  /** Init a Async Remote Server Plugin with an async server endpoint */
  void initRemotePluginWithAsyncNode(AsyncEndpointServer serverEndpoint) {
    try {
      remotePlugin = RemoteServerUtils.getRemotePlugin();
      logger.info("RemotePluginServer already registered, reusing it");
    } catch (KeyplePluginNotFoundException e) {
      remotePlugin =
          (RemoteServerPlugin)
              SmartCardService.getInstance()
                  .registerPlugin(
                      RemoteServerPluginFactory.builder()
                          .withAsyncNode(serverEndpoint)
                          .withPluginObserver(new RemotePluginObserver())
                          .usingEventNotificationPool(serverPool)
                          .build());
    }
  }

  StubCalypsoClassic getSlowCard() {
    return new StubCalypsoClassic() {
      @Override
      public byte[] processApdu(byte[] apduIn) {
        try {
          logger.warn("Simulate a slow Card by sleeping 1 second before sending response");
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return super.processApdu(apduIn);
      }
    };
  }
  /**
   * Perform a calypso PO selection
   *
   * @return matching PO
   */
  CalypsoPo explicitPoSelection() {
    CardSelection seSelection = CalypsoUtilities.getSeSelection();
    SelectionsResult selectionsResult = seSelection.processExplicitSelection(localReader);
    return (CalypsoPo) selectionsResult.getActiveSmartCard();
  }

  Callable<Boolean> verifyUserTransaction(
      final ReaderEventFilter eventFilter, final UserInput userInput, final boolean isSucessful) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return eventFilter.transactionResult != null
            && eventFilter.transactionResult.isSuccessful() == isSucessful
            && eventFilter.transactionResult.getUserId().equals(userInput.getUserId())
            && eventFilter.resetTransactionResult();
      }
    };
  }

  Callable<Boolean> seRemoved(final Reader seReader) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return !seReader.isCardPresent();
      }
    };
  }

  Callable<Boolean> executeTransaction(
      final LocalServiceClient localService,
      final StubReader localReader,
      final UserInput user) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        // insert stub card into stub
        localReader.insertCard(new StubCalypsoClassic());

        // execute remote service
        TransactionResult output =
            localService.executeRemoteService(
                RemoteServiceParameters.builder(SERVICE_ID_3, localReader)
                    .withUserInputData(user)
                    .build(),
                TransactionResult.class);

        // validate result
        assertThat(output.isSuccessful()).isTrue();
        assertThat(output.getUserId()).isEqualTo(user.getUserId());
        return true;
      }
    };
  }

  void localselection_remoteTransaction_successful() {
    // insert stub card into stub
    localReader.insertCard(new StubCalypsoClassic());

    // execute a local selection on local reader
    CalypsoPo calypsoPo = explicitPoSelection();

    // execute remote service fed with the card
    TransactionResult output =
        localService.executeRemoteService(
            RemoteServiceParameters.builder(SERVICE_ID_1, localReader)
                .withInitialSeContext(calypsoPo)
                .withUserInputData(user1)
                .build(),
            TransactionResult.class);

    // validate result
    assertThat(output.isSuccessful()).isTrue();
    assertThat(output.getUserId()).isEqualTo(user1.getUserId());
  }

  void remoteselection_remoteTransaction_successful() {
    // insert stub card into stub
    localReader.insertCard(new StubCalypsoClassic());

    // execute remote service
    TransactionResult output =
        localService.executeRemoteService(
            RemoteServiceParameters.builder(SERVICE_ID_3, localReader)
                .withUserInputData(user1)
                .build(),
            TransactionResult.class);

    // validate result
    assertThat(output.isSuccessful()).isTrue();
    assertThat(output.getUserId()).isEqualTo(user1.getUserId());
  }

  void multipleclients_remoteselection_remoteTransaction_successful() {
    user2 = new UserInput().setUserId("user2");

    // insert stub card into both readers
    localReader.insertCard(new StubCalypsoClassic());
    localReader2.insertCard(new StubCalypsoClassic());

    // execute remoteservice task concurrently on both readers
    final Future<Boolean> task1 =
        clientPool.submit(executeTransaction(localService, localReader, user1));
    final Future<Boolean> task2 =
        clientPool.submit(executeTransaction(localService, localReader2, user2));

    // wait for termination
    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(
            new Callable<Boolean>() {
              @Override
              public Boolean call() throws Exception {
                return task1.isDone() && task2.isDone() && task1.get() && task2.get();
              }
            });
  }

  void transaction_closeSession_fail() {
    StubCalypsoClassic failingSe = new StubCalypsoClassic();
    // remove read record command to make the tx fail
    failingSe.removeHexCommand("00B2014400");

    localReader.insertCard(failingSe);

    // execute remote service
    TransactionResult output =
        localService.executeRemoteService(
            RemoteServiceParameters.builder(SERVICE_ID_3, localReader)
                .withUserInputData(user1)
                .build(),
            TransactionResult.class);

    // validate result is false
    assertThat(output.isSuccessful()).isFalse();
    assertThat(output.getUserId()).isEqualTo(user1.getUserId());
  }

  void transaction_slowSe_success() {

    localReader.insertCard(getSlowCard());

    try {
      // execute remote service
      TransactionResult output =
          localService.executeRemoteService(
              RemoteServiceParameters.builder(SERVICE_ID_3, localReader)
                  .withUserInputData(user1)
                  .build(),
              TransactionResult.class);

      // validate result is false
      assertThat(output.isSuccessful()).isTrue();
      assertThat(output.getUserId()).isEqualTo(user1.getUserId());
    } catch (RuntimeException e) {
      assertThat(e).isNull();
    }
  }

  void defaultSelection_onMatched_transaction_successful(ReaderEventFilter eventFilter) {
    // execute remote service to create observable virtual reader
    ConfigurationResult configurationResult =
        localService.executeRemoteService(
            RemoteServiceParameters.builder(SERVICE_ID_2, localReader)
                .withUserInputData(device1)
                .build(),
            ConfigurationResult.class);

    assertThat(configurationResult.isSuccessful()).isTrue();
    assertThat(configurationResult.getDeviceId()).isEqualTo(device1.getDeviceId());

    eventFilter.setUserData(user1);

    /*
     * user1 inserts card ,
     * card event is sent to server,
     * a transaction is operated in response
     * user1 removes card
     */
    localReader.insertCard(new StubCalypsoClassic());
    logger.info(
        "1 - Verify User Transaction is successful for first user {}",
        eventFilter.user.getUserId());
    await().atMost(10, TimeUnit.SECONDS).until(verifyUserTransaction(eventFilter, user1, true));
    localReader.removeCard();
    await().atMost(1, TimeUnit.SECONDS).until(seRemoved(localReader));

    /*
     * user2 inserts card ,
     * card event is sent to server,
     * a transaction is operated in response
     * user2 removes card
     */
    UserInput user2 = new UserInput().setUserId(UUID.randomUUID().toString());
    eventFilter.setUserData(user2);
    localReader.insertCard(new StubCalypsoClassic());
    logger.info(
        "2 - Verify User Transaction is successful for second user {}",
        eventFilter.user.getUserId());
    await().atMost(10, TimeUnit.SECONDS).until(verifyUserTransaction(eventFilter, user2, true));
    localReader.removeCard();
    await().atMost(1, TimeUnit.SECONDS).until(seRemoved(localReader));

    /*
     * on the 2nd event, the virtual reader should be cleaned on local and virtual environment
     */
    assertThat(remotePlugin.getReaders()).isEmpty();
    assertThat(LocalServiceClientTest.getVirtualReaders(localService)).isEmpty();
  }

  void remoteselection_remoteTransaction() {
    // insert stub card into stub
    localReader.insertCard(new StubCalypsoClassic());

    // execute remote service
    TransactionResult output =
        localService.executeRemoteService(
            RemoteServiceParameters.builder(SERVICE_ID_3, localReader)
                .withUserInputData(user1)
                .build(),
            TransactionResult.class);
  }

  void all_methods() {
    // insert stub card into stub
    localReader.insertCard(new StubCalypsoClassic());

    // execute remote service
    TransactionResult output =
        localService.executeRemoteService(
            RemoteServiceParameters.builder(SERVICE_ID_4, localReader)
                .withUserInputData(device1)
                .build(),
            TransactionResult.class);

    // validate result
    assertThat(output.isSuccessful()).isTrue();
  }

  void setTimeoutInNode(AbstractNode node, Integer timeoutInMilliseconds) {
    try {
      Class classT = node.getClass();
      Field field = classT.getDeclaredField("timeout");
      field.setAccessible(true);
      field.set(node, timeoutInMilliseconds);
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
