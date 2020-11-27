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
package org.eclipse.keyple.core.service.event;

/**
 * This interface must be implemented by any application using the plugin observation.
 *
 * <p>It provides a channel for notifying runtime exceptions that may occur during operations
 * carried out by the monitoring thread(s).<br>
 * These exceptions can be thrown either in the internal monitoring layers of the readers or in the
 * application itself (within the update methods when notifying plugin observers for example).
 *
 * @since 1.0
 */
public interface PluginObservationExceptionHandler {
  /**
   * This method is invoked when a runtime exception occurs in the observed plugin.
   *
   * @param pluginName A not empty String containing the name of the plugin in which the exception
   *     was raised.
   * @param e A reference to the {@link RuntimeException}
   * @since 1.0
   */
  void onPluginObservationError(String pluginName, Throwable e);
}
