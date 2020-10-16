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
package org.eclipse.keyple.plugin.remote.core.exception;

import org.eclipse.keyple.core.seproxy.exception.KeypleException;

/**
 * Used in KeypleClientReaderEventFilter to prevent the event from being sent to the VirtualSePlugin
 */
public class KeypleDoNotPropagateEventException extends KeypleException {

  public KeypleDoNotPropagateEventException(String message) {
    super(message);
  }
}
