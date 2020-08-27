/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.example.common.generic;

import static org.eclipse.keyple.core.seproxy.protocol.TransmissionMode.*;

import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode.*;

/** Custom protocol definitions to illustrate the extension of the Keyple SDK definitions */
public enum CustomProtocols implements SeProtocol {
  CUSTOM_PROTOCOL_B_PRIME("Custom Old Calypso B prime", CONTACTLESS),

  CUSTOM_PROTOCOL_MIFARE_DESFIRE("Custom Mifare DESFire", CONTACTLESS);

  /** The protocol name. */
  private final String name;

  private final TransmissionMode transmissionMode;

  CustomProtocols(String name, TransmissionMode transmissionMode) {
    this.name = name;
    this.transmissionMode = transmissionMode;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public TransmissionMode getTransmissionMode() {
    return transmissionMode;
  }
}
