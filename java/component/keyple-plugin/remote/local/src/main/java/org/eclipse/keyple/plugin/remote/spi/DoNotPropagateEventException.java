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
 * This exception is thrown to indicate to stop the propagation of an event to the remote node.
 *
 * <p>You must thrown this exception during the invocation of the method {@link
 * ObservableReaderEventFilter#beforePropagation(ReaderEvent)}.
 *
 * @since 1.0
 */
public class DoNotPropagateEventException extends RuntimeException {

  /**
   * @param message The message to identify the exception context.
   * @since 1.0
   */
  public DoNotPropagateEventException(String message) {
    super(message);
  }
}
