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
package org.eclipse.keyple.plugin.remotese.integration.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.UUID;
import java.util.concurrent.*;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.plugin.remotese.core.KeypleServerAsync;
import org.eclipse.keyple.plugin.remotese.integration.common.app.ReaderEventFilter;
import org.eclipse.keyple.plugin.remotese.integration.common.app.RemoteSePluginObserver;
import org.eclipse.keyple.plugin.remotese.integration.common.model.ConfigurationResult;
import org.eclipse.keyple.plugin.remotese.integration.common.model.DeviceInput;
import org.eclipse.keyple.plugin.remotese.integration.common.model.TransactionResult;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remotese.integration.common.se.StubCalypsoClassic;
import org.eclipse.keyple.plugin.remotese.integration.common.util.CalypsoUtilities;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;
import org.eclipse.keyple.plugin.remotese.nativese.RemoteServiceParameters;
import org.eclipse.keyple.plugin.remotese.nativese.impl.NativeSeClientServiceTest;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerPlugin;
import org.eclipse.keyple.plugin.remotese.virtualse.impl.RemoteSeServerPluginFactory;
import org.eclipse.keyple.plugin.remotese.virtualse.impl.RemoteSeServerUtils;
import org.eclipse.keyple.plugin.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseScenario {

  public interface IntegrationScenario {

    /**
     * A successful aid selection is executed locally on the terminal followed by a remoteService call
     * to launch the remote Calypso session. The SE content is sent during this first called along
     * with custom data. All this information is received by the server to select and execute the
     * corresponding ticketing scenario.
     *
     * <p>At the end of a successful calypso session, custom data is sent back to the client as a
     * final result.
     *
     * <p>This scenario can be executed on Sync node and Async node.
     */
    void execute1_localselection_remoteTransaction_successful();


    /**
     * The client application invokes the remoteService with enabling observability capabilities. As a
     * result the server creates a Observable Virtual Reader that receives native reader events such
     * as SE insertions and removals.
     *
     * <p>A SE Insertion is simulated locally followed by a SE removal 1 second later.
     *
     * <p>The SE Insertion event is sent to the Virtual Reader whose observer starts a remote Calypso
     * session. At the end of a successful calypso session, custom data is sent back to the client as
     * a final result.
     *
     * <p>The operation is executed twice with two different users.
     *
     * <p>After the second SE insertion, Virtual Reader observers are cleared to purge the server
     * virtual reader.
     */
    void execute2_defaultSelection_onMatched_transaction_successful();


    /**
     * Similar to scenario 1 without the local aid selection. In this case, the server application is
     * responsible for ordering the aid selection.
     */
    void execute3_remoteselection_remoteTransaction_successful();

    /** Similar to scenario 3 with two concurrent clients. */
    void execute4_multiclient_remoteselection_remoteTransaction_successful();
  }

  private static final Logger logger = LoggerFactory.getLogger(BaseScenario.class);

  public static String NATIVE_PLUGIN_NAME = "stubPlugin";
  public static String NATIVE_READER_NAME = "stubReader";
  public static String NATIVE_READER_NAME_2 = "stubReader2";

  public static String SERVICE_ID_1 = "EXECUTE_CALYPSO_SESSION_FROM_LOCAL_SELECTION";
  public static String SERVICE_ID_3 = "EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION";
  public static String SERVICE_ID_2 = "CREATE_CONFIGURE_OBS_VIRTUAL_READER";

  public static String DEVICE_ID = "Xo99";

  NativeSeClientService nativeService;
  StubPlugin nativePlugin;
  StubReader nativeReader;
  StubReader nativeReader2;

  RemoteSeServerPlugin remoteSePlugin;
  UserInput user1;
  UserInput user2;
  DeviceInput device1;

  ExecutorService threadPool = Executors.newCachedThreadPool();

  /** Init native stub plugin that can work with {@link StubSecureElement} */
  void initNativeStubPlugin() {
    // reuse stub plugin
    try {
      nativePlugin = (StubPlugin) SeProxyService.getInstance().getPlugin(NATIVE_PLUGIN_NAME);
    } catch (KeyplePluginNotFoundException e) {
      nativePlugin =
          (StubPlugin)
              SeProxyService.getInstance()
                  .registerPlugin(new StubPluginFactory(NATIVE_PLUGIN_NAME));
    }
    // nativeReader should be reset
    try {
      nativeReader = (StubReader) nativePlugin.getReader(NATIVE_READER_NAME);
      assertThat(nativeReader).isNull();
    } catch (KeypleReaderNotFoundException e) {
      nativePlugin.plugStubReader(NATIVE_READER_NAME, true);
      nativeReader = (StubReader) nativePlugin.getReader(NATIVE_READER_NAME);
      // configure the procotol settings
      nativeReader.addSeProtocolSetting(
          SeCommonProtocols.PROTOCOL_ISO14443_4,
          StubProtocolSetting.STUB_PROTOCOL_SETTING.get(SeCommonProtocols.PROTOCOL_ISO14443_4));
    }
// nativeReader should be reset
    try {
      nativeReader2 = (StubReader) nativePlugin.getReader(NATIVE_READER_NAME_2);
      assertThat(nativeReader2).isNull();
    } catch (KeypleReaderNotFoundException e) {
      // plug a second reader
      nativePlugin.plugStubReader(NATIVE_READER_NAME_2, true);
      nativeReader2 = (StubReader) nativePlugin.getReader(NATIVE_READER_NAME_2);
      nativeReader2.addSeProtocolSetting(
              SeCommonProtocols.PROTOCOL_ISO14443_4,
              StubProtocolSetting.STUB_PROTOCOL_SETTING.get(SeCommonProtocols.PROTOCOL_ISO14443_4));
    }


    }

  void clearNativeReader() {
    nativePlugin.unplugStubReader(NATIVE_READER_NAME, true);
    nativePlugin.unplugStubReader(NATIVE_READER_NAME_2, true);
  }

  /** Init a Sync Remote Se Server Plugin (ie. http server) */
  void initRemoteSePluginWithSyncNode() {
    try {
      remoteSePlugin = RemoteSeServerUtils.getSyncPlugin();
    } catch (KeyplePluginNotFoundException e) {
      remoteSePlugin =
          (RemoteSeServerPlugin)
              SeProxyService.getInstance()
                  .registerPlugin(
                      RemoteSeServerPluginFactory.builder()
                          .withSyncNode()
                          .withPluginObserver(new RemoteSePluginObserver())
                          .usingDefaultEventNotificationPool()
                          .build());
    }
  }

  /** Init a Async Remote Se Server Plugin with an async server endpoint */
  void initRemoteSePluginWithAsyncNode(KeypleServerAsync serverEndpoint) {
    try {
      remoteSePlugin = RemoteSeServerUtils.getAsyncPlugin();
      logger.info("RemoteSePluginServer already registered, reusing it");
    } catch (KeyplePluginNotFoundException e) {
      remoteSePlugin =
          (RemoteSeServerPlugin)
              SeProxyService.getInstance()
                  .registerPlugin(
                      RemoteSeServerPluginFactory.builder()
                          .withAsyncNode(serverEndpoint)
                          .withPluginObserver(new RemoteSePluginObserver())
                          .usingDefaultEventNotificationPool()
                          .build());
    }
  }

  /**
   * Perform a calypso PO selection
   *
   * @return matching PO
   */
  CalypsoPo explicitPoSelection() {
    SeSelection seSelection = CalypsoUtilities.getSeSelection();
    SelectionsResult selectionsResult = seSelection.processExplicitSelection(nativeReader);
    return (CalypsoPo) selectionsResult.getActiveMatchingSe();
  }

  Callable<Boolean> verifyUserTransaction(
      final ReaderEventFilter eventFilter, final UserInput userInput) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return eventFilter.transactionResult != null
            && eventFilter.transactionResult.isSuccessful()
            && eventFilter.transactionResult.getUserId().equals(userInput.getUserId())
            && eventFilter.resetTransactionResult();
      }
    };
  }

  Callable<Boolean> seRemoved(final SeReader seReader) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return !seReader.isSePresent();
      }
    };
  }

  Callable<Boolean> executeTransaction(
      final NativeSeClientService nativeService,
      final StubReader nativeReader,
      final UserInput user) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        // insert stub SE into stub
        nativeReader.insertSe(new StubCalypsoClassic());

        // execute remote service
        TransactionResult output =
            nativeService.executeRemoteService(
                RemoteServiceParameters.builder(SERVICE_ID_3, nativeReader)
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
    // insert stub SE into stub
    nativeReader.insertSe(new StubCalypsoClassic());

    // execute a local selection on native reader
    CalypsoPo calypsoPo = explicitPoSelection();

    // execute remote service fed with the SE
    TransactionResult output =
        nativeService.executeRemoteService(
            RemoteServiceParameters.builder(SERVICE_ID_1, nativeReader)
                .withInitialSeContext(calypsoPo)
                .withUserInputData(user1)
                .build(),
            TransactionResult.class);

    // validate result
    assertThat(output.isSuccessful()).isTrue();
    assertThat(output.getUserId()).isEqualTo(user1.getUserId());
  }

  void defaultSelection_onMatched_transaction_successful(ReaderEventFilter eventFilter) {
    // execute remote service to create observable virtual reader
    ConfigurationResult configurationResult =
        nativeService.executeRemoteService(
            RemoteServiceParameters.builder(SERVICE_ID_2, nativeReader)
                .withUserInputData(device1)
                .build(),
            ConfigurationResult.class);

    assertThat(configurationResult.isSuccessful()).isTrue();
    assertThat(configurationResult.getDeviceId()).isEqualTo(device1.getDeviceId());

    eventFilter.setUserData(user1);

    /*
     * user1 inserts SE ,
     * SE event is sent to server,
     * a transaction is operated in response
     * user1 removes SE
     */
    nativeReader.insertSe(new StubCalypsoClassic());
    logger.info(
        "1 - Verify User Transaction is successful for first user {}",
        eventFilter.user.getUserId());
    await().atMost(10, TimeUnit.SECONDS).until(verifyUserTransaction(eventFilter, user1));
    nativeReader.removeSe();
    await().atMost(1, TimeUnit.SECONDS).until(seRemoved(nativeReader));

    /*
     * user2 inserts SE ,
     * SE event is sent to server,
     * a transaction is operated in response
     * user2 removes SE
     */
    UserInput user2 = new UserInput().setUserId(UUID.randomUUID().toString());
    eventFilter.setUserData(user2);
    nativeReader.insertSe(new StubCalypsoClassic());
    logger.info(
        "2 - Verify User Transaction is successful for second user {}",
        eventFilter.user.getUserId());
    await().atMost(10, TimeUnit.SECONDS).until(verifyUserTransaction(eventFilter, user2));
    nativeReader.removeSe();
    await().atMost(1, TimeUnit.SECONDS).until(seRemoved(nativeReader));

    /*
     * on the 2nd event, the virtual reader should be cleaned on native and virtual environment
     */
    assertThat(remoteSePlugin.getReaders()).isEmpty();
    assertThat(NativeSeClientServiceTest.getVirtualReaders(nativeService)).isEmpty();
  }

  void remoteselection_remoteTransaction_successful() {
    // insert stub SE into stub
    nativeReader.insertSe(new StubCalypsoClassic());

    // execute remote service
    TransactionResult output =
        nativeService.executeRemoteService(
            RemoteServiceParameters.builder(SERVICE_ID_3, nativeReader)
                .withUserInputData(user1)
                .build(),
            TransactionResult.class);

    // validate result
    assertThat(output.isSuccessful()).isTrue();
    assertThat(output.getUserId()).isEqualTo(user1.getUserId());
  }

  void multipleclients_remoteselection_remoteTransaction_successful() {
    user2 = new UserInput().setUserId("user2");

    // insert stub SE into both readers
    nativeReader.insertSe(new StubCalypsoClassic());
    nativeReader2.insertSe(new StubCalypsoClassic());

    // execute remoteservice task concurrently on both readers
    final Future<Boolean> task1 =
        threadPool.submit(executeTransaction(nativeService, nativeReader, user1));
    final Future<Boolean> task2 =
        threadPool.submit(executeTransaction(nativeService, nativeReader2, user2));

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
}
