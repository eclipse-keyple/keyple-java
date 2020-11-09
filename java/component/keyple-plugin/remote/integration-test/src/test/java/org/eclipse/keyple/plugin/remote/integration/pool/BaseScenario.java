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
import org.eclipse.keyple.plugin.remote.integration.common.se.StubCalypsoClassic;
import org.eclipse.keyple.plugin.remote.nativ.NativePoolServerService;
import org.eclipse.keyple.plugin.remote.virtual.RemotePoolClientPlugin;
import org.eclipse.keyple.plugin.stub.*;

public abstract class BaseScenario {

  private static final String NATIVE_POOL_PLUGIN_NAME = "stubPoolPlugin";
  private static final String NATIVE_READER_NAME = "stubReader";
  private static final String NATIVE_READER_NAME_2 = "stubReader2";
  protected StubPoolPlugin nativePoolPlugin;
  protected StubReader nativeReader;
  protected StubReader nativeReader2;
  protected String groupReference = "groupReference";

  protected NativePoolServerService nativePoolServerService;
  protected RemotePoolClientPlugin remotePoolClientPlugin;

  abstract void execute_transaction_on_pool_reader();

  /** Init native stub plugin that can work with {@link StubSecureElement} */
  void initNativePoolStubPlugin() {
    // reuse stub plugin
    try {
      nativePoolPlugin =
          (StubPoolPlugin) SmartCardService.getInstance().getPlugin(NATIVE_POOL_PLUGIN_NAME);
    } catch (KeyplePluginNotFoundException e) {
      nativePoolPlugin =
          (StubPoolPlugin)
              SmartCardService.getInstance()
                  .registerPlugin(new StubPoolPluginFactory(NATIVE_POOL_PLUGIN_NAME));
    }
    // plug one reader if not exists yet
    try {
      nativeReader = (StubReader) nativePoolPlugin.getReader(NATIVE_READER_NAME);
    } catch (KeypleReaderNotFoundException e) {
      nativePoolPlugin.plugStubPoolReader(
          groupReference, NATIVE_READER_NAME, new StubCalypsoClassic());
      nativeReader = (StubReader) nativePoolPlugin.getReader(NATIVE_READER_NAME);
      // activate ISO_14443_4
      nativeReader.activateProtocol(
          StubSupportedProtocols.ISO_14443_4.name(),
          ContactlessCardCommonProtocols.ISO_14443_4.name());
    }
    // plug another reader if not exists yet
    try {
      nativeReader2 = (StubReader) nativePoolPlugin.getReader(NATIVE_READER_NAME_2);
    } catch (KeypleReaderNotFoundException e) {
      // plug a second reader
      nativePoolPlugin.plugStubPoolReader(
          groupReference, NATIVE_READER_NAME_2, new StubCalypsoClassic());
      nativeReader2 = (StubReader) nativePoolPlugin.getReader(NATIVE_READER_NAME_2);
      // activate ISO_14443_4
      nativeReader2.activateProtocol(
          StubSupportedProtocols.ISO_14443_4.name(),
          ContactlessCardCommonProtocols.ISO_14443_4.name());
    }
  }
}
