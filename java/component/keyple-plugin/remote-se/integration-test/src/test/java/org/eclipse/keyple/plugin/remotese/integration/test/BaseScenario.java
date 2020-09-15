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

import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.plugin.remotese.core.KeypleServerAsync;
import org.eclipse.keyple.plugin.remotese.integration.common.util.CalypsoClassicInfo;
import org.eclipse.keyple.plugin.remotese.integration.common.app.RemoteSePluginObserver;
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

  public static String SERVICE_ID_1 = "EXECUTE_CALYPSO_SESSION_FROM_SELECTION";
  public static String USER_ID = "Alexandre3";

  StubPlugin nativePlugin;
  StubReader nativeReader;

  RemoteSeServerPlugin remoteSePlugin;

  /**
   * Init native stub plugin that can work with {@link StubSecureElement}
   */
  void initNativeStubPlugin() {
    try {
      nativePlugin = (StubPlugin) SeProxyService.getInstance().getPlugin(NATIVE_PLUGIN_NAME);
    } catch (KeyplePluginNotFoundException e) {
      nativePlugin =
          (StubPlugin)
              SeProxyService.getInstance()
                  .registerPlugin(new StubPluginFactory(NATIVE_PLUGIN_NAME));
    }
    try {
      nativeReader = (StubReader) nativePlugin.getReader(NATIVE_READER_NAME);
    } catch (KeypleReaderNotFoundException e) {
      nativePlugin.plugStubReader(NATIVE_READER_NAME, true);
      nativeReader = (StubReader) nativePlugin.getReader(NATIVE_READER_NAME);
    }
    // configure the procotol settings
    nativeReader.addSeProtocolSetting(
        SeCommonProtocols.PROTOCOL_ISO14443_4,
        StubProtocolSetting.STUB_PROTOCOL_SETTING.get(SeCommonProtocols.PROTOCOL_ISO14443_4));
  }

  /**
   * Init a Sync Remote Se Server Plugin (ie. http server)
   */
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
                          .withPluginObserver(new RemoteSePluginObserver( true))
                          .usingDefaultEventNotificationPool()
                          .build());
    }
  }

  /**
   * Init a Async Remote Se Server Plugin with an async server endpoint
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
                          .withPluginObserver(new RemoteSePluginObserver( false))
                          .usingDefaultEventNotificationPool()
                          .build());
    }
  }

  /**
   * Perform a calypso PO selection
   * @return matching PO
   */
  CalypsoPo explicitPoSelection() {
    // Prepare PO Selection
    SeSelection seSelection = new SeSelection();

    // Calypso selection
    PoSelectionRequest poSelectionRequest =
        new PoSelectionRequest(
            PoSelector.builder()
                .seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4)
                .aidSelector(
                    SeSelector.AidSelector.builder().aidToSelect(CalypsoClassicInfo.AID).build())
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                .build());

    // Prepare the reading order.
    poSelectionRequest.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_EnvironmentAndHolder, CalypsoClassicInfo.RECORD_NUMBER_1);

    // Add the selection case to the current selection
    seSelection.prepareSelection(poSelectionRequest);
    SelectionsResult selectionsResult = seSelection.processExplicitSelection(nativeReader);
    return (CalypsoPo) selectionsResult.getActiveMatchingSe();
  }
}
