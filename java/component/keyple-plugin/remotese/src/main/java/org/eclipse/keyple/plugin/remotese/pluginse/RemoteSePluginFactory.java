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
package org.eclipse.keyple.plugin.remotese.pluginse;

import java.util.concurrent.ExecutorService;
import org.eclipse.keyple.core.plugin.factory.PluginFactory;
import org.eclipse.keyple.core.reader.Plugin;
import org.eclipse.keyple.core.reader.exception.KeyplePluginInstantiationException;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;

/** Used internally by MasterAPI to create the {@link RemoteSePlugin} */
class RemoteSePluginFactory implements PluginFactory {

  VirtualReaderSessionFactory sessionManager;
  DtoSender dtoSender;
  long rpc_timeout;
  String pluginName;
  ExecutorService executorService;

  public RemoteSePluginFactory(
      VirtualReaderSessionFactory sessionManager,
      DtoSender dtoSender,
      long rpc_timeout,
      String pluginName,
      ExecutorService executorService) {
    this.sessionManager = sessionManager;
    this.dtoSender = dtoSender;
    this.rpc_timeout = rpc_timeout;
    this.pluginName = pluginName;
    this.executorService = executorService;
  }

  @Override
  public String getPluginName() {
    return pluginName;
  }

  public Plugin getPlugin() {
    try {
      return new RemoteSePluginImpl(
          sessionManager, dtoSender, rpc_timeout, pluginName, executorService);
    } catch (Exception e) {
      throw new KeyplePluginInstantiationException("Can not access RemoteSe readers", e);
    }
  }
}
