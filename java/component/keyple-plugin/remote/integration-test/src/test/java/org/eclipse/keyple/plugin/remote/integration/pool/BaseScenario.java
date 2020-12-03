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
package org.eclipse.keyple.plugin.remote.integration.pool;

import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.plugin.remote.PoolLocalServiceServer;
import org.eclipse.keyple.plugin.remote.PoolRemotePluginClient;
import org.eclipse.keyple.plugin.remote.integration.common.se.StubCalypsoClassic;
import org.eclipse.keyple.plugin.stub.*;

public abstract class BaseScenario {

  private static final String NATIVE_POOL_PLUGIN_NAME = "stubPoolPlugin";
  private static final String NATIVE_READER_NAME = "stubReader";
  private static final String NATIVE_READER_NAME_2 = "stubReader2";
  protected StubPoolPlugin localPoolPlugin;
  protected StubReader localReader;
  protected StubReader localReader2;
  protected String groupReference = "groupReference";

  protected PoolLocalServiceServer poolLocalServiceServer;
  protected PoolRemotePluginClient poolRemotePluginClient;

  protected String localServiceName;

  abstract void execute_transaction_on_pool_reader();

  /** Init local stub plugin that can work with {@link StubSmartCard} */
  void initNativePoolStubPlugin() {
    // reuse stub plugin
    try {
      localPoolPlugin =
          (StubPoolPlugin) SmartCardService.getInstance().getPlugin(NATIVE_POOL_PLUGIN_NAME);
    } catch (KeyplePluginNotFoundException e) {
      localPoolPlugin =
          (StubPoolPlugin)
              SmartCardService.getInstance()
                  .registerPlugin(new StubPoolPluginFactory(NATIVE_POOL_PLUGIN_NAME, null, null));
    }
    // plug one reader if not exists yet
    try {
      localReader = (StubReader) localPoolPlugin.getReader(NATIVE_READER_NAME);
    } catch (KeypleReaderNotFoundException e) {
      localPoolPlugin.plugPoolReader(groupReference, NATIVE_READER_NAME, new StubCalypsoClassic());
      localReader = (StubReader) localPoolPlugin.getReader(NATIVE_READER_NAME);
      // activate ISO_14443_4
      localReader.activateProtocol(
          StubSupportedProtocols.ISO_14443_4.name(),
          ContactlessCardCommonProtocols.ISO_14443_4.name());
    }
    // plug another reader if not exists yet
    try {
      localReader2 = (StubReader) localPoolPlugin.getReader(NATIVE_READER_NAME_2);
    } catch (KeypleReaderNotFoundException e) {
      // plug a second reader
      localPoolPlugin.plugPoolReader(
          groupReference, NATIVE_READER_NAME_2, new StubCalypsoClassic());
      localReader2 = (StubReader) localPoolPlugin.getReader(NATIVE_READER_NAME_2);
      // activate ISO_14443_4
      localReader2.activateProtocol(
          StubSupportedProtocols.ISO_14443_4.name(),
          ContactlessCardCommonProtocols.ISO_14443_4.name());
    }
  }
}
