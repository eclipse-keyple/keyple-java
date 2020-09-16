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

import java.util.concurrent.Callable;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientReaderEventFilter;
import org.eclipse.keyple.plugin.remotese.core.KeypleServerAsync;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleDoNotPropagateEventException;
import org.eclipse.keyple.plugin.remotese.integration.common.app.RemoteSePluginObserver;
import org.eclipse.keyple.plugin.remotese.integration.common.model.TransactionResult;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remotese.integration.common.util.CalypsoUtilities;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerPlugin;
import org.eclipse.keyple.plugin.remotese.virtualse.impl.RemoteSeServerPluginFactory;
import org.eclipse.keyple.plugin.remotese.virtualse.impl.RemoteSeServerUtils;
import org.eclipse.keyple.plugin.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseScenario {

  private static final Logger logger = LoggerFactory.getLogger(BaseScenario.class);

  public static String NATIVE_PLUGIN_NAME = "stubPlugin";
  public static String NATIVE_READER_NAME = "stubReader";

  public static String SERVICE_ID_1 = "EXECUTE_CALYPSO_SESSION_FROM_LOCAL_SELECTION";
  public static String SERVICE_ID_2 = "CREATE_CONFIGURE_OBS_VIRTUAL_READER";

  public static String USER_ID = "Alexandre3";
  public static String DEVICE_ID = "Xo99";

  StubPlugin nativePlugin;
  StubReader nativeReader;

  RemoteSeServerPlugin remoteSePlugin;
  UserInput user1;

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
  }

  void clearNativeReader() {
    nativePlugin.unplugStubReader(NATIVE_READER_NAME, true);
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

  /**
   * Init a Async Remote Se Server Plugin with an async server endpoint
   *
   * @param asyncServerEndpoint async server endpoint (ie. websocket server)
   */
  void initRemoteSePluginWithAsyncNode(KeypleServerAsync asyncServerEndpoint) {
    try {
      remoteSePlugin = RemoteSeServerUtils.getSyncPlugin();
    } catch (KeyplePluginNotFoundException e) {
      remoteSePlugin =
          (RemoteSeServerPlugin)
              SeProxyService.getInstance()
                  .registerPlugin(
                      RemoteSeServerPluginFactory.builder()
                          .withAsyncNode(asyncServerEndpoint)
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
            && eventFilter.transactionResult.getUserId().equals(userInput.getUserId());
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

  class ReaderEventFilter implements KeypleClientReaderEventFilter {
    TransactionResult transactionResult;
    UserInput user;

    public void setUser(UserInput user) {
      this.user = user;
    }

    @Override
    public Object beforePropagation(ReaderEvent event) throws KeypleDoNotPropagateEventException {
      switch (event.getEventType()) {
        case SE_MATCHED:
          return new UserInput().setUserId(user.getUserId());
        case SE_REMOVED:
        case SE_INSERTED:
        default:
          throw new KeypleDoNotPropagateEventException("only SE Matched is propagated");
      }
    }

    @Override
    public Class<? extends Object> getUserOutputDataClass() {
      return TransactionResult.class;
    }

    @Override
    public void afterPropagation(Object userOutputData) {
      transactionResult = (TransactionResult) userOutputData;
    }
  };
}
