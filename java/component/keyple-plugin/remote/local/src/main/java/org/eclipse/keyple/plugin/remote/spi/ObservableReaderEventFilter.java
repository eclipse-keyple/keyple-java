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
package org.eclipse.keyple.plugin.remote.spi;

import org.eclipse.keyple.core.service.event.ReaderEvent;

/**
 * SPI of the <b>local filter</b> associated to an <b>observable reader event</b>.
 *
 * <p>You must provide an implementation of this interface if you use a {@link
 * org.eclipse.keyple.plugin.remote.LocalServiceClient} and you plan to observe remotely the local
 * reader.
 *
 * @since 1.0
 */
public interface ObservableReaderEventFilter {

  /**
   * This method is invoked when a reader event occurs, before propagating it to the server.
   *
   * <p>Then, you have the possibility to :
   *
   * <ul>
   *   <li>execute a specific treatment,
   *   <li>return if necessary a DTO to be transmitted to the remote service,
   *   <li>stop the propagation of the event by throwing the exception {@link
   *       DoNotPropagateEventException}.
   * </ul>
   *
   * @param event The reader event.
   * @return The user input data of the remote service or <b>null</b> if you don't have any data to
   *     transmit to the server.
   * @throws DoNotPropagateEventException if you want to stop the propagation of the event.
   * @since 1.0
   */
  Object beforePropagation(ReaderEvent event);

  /**
   * This method must return the class of the user output data expected at the output of the remote
   * service.
   *
   * <p>This method is invoked in order to deserialize the user output data before to invoke the
   * method {@link #afterPropagation(Object)}.
   *
   * @return <b>null</b> if there is no user output data to deserialize.
   * @since 1.0
   */
  Class<? extends Object> getUserOutputDataClass();

  /**
   * This method is invoked at the end of the processing of the remote service to deliver the
   * result.
   *
   * @param userOutputData The user output data previously deserialized using the method {@link
   *     #getUserOutputDataClass()}, or <b>null</b> if there is no data.
   * @since 1.0
   */
  void afterPropagation(Object userOutputData);
}
