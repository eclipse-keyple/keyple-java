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
package org.eclipse.keyple.plugin.remote;

import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.plugin.remote.exception.KeypleDoNotPropagateEventException;

/**
 * Observable Reader Reader Event Filter<br>
 * This interface should be implemented by the user in the use case of the Remote Server Plugin when
 * the reader observation is activated.
 *
 * @since 1.0
 */
public interface ObservableReaderEventFilter {

  /**
   * Execute any process before the event is sent to the server
   *
   * @param event that will be propagated
   * @return nullable data that will be sent to the server.
   * @throws KeypleDoNotPropagateEventException if event should not be propagated to server
   * @since 1.0
   */
  Object beforePropagation(ReaderEvent event) throws KeypleDoNotPropagateEventException;

  /**
   * Return the class of the user output data.<br>
   * This method is used internally to deserialize the user output data before to call the method
   * {@link ObservableReaderEventFilter#afterPropagation(Object)}.
   *
   * @return null if there is no user output data to deserialize.
   * @since 1.0
   */
  Class<? extends Object> getUserOutputDataClass();

  /**
   * Retrieve the output from the event global processing
   *
   * @param userOutputData The user output data previously deserialized using the method {@link
   *     ObservableReaderEventFilter#getUserOutputDataClass()}
   * @since 1.0
   */
  void afterPropagation(Object userOutputData);
}
