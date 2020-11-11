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
package org.eclipse.keyple.example.common.generic.stub;

import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.plugin.stub.StubSmartCard;

/** Simple contact card Stub (no command) */
public class StubCalypsoBPrime extends StubSmartCard {

  static final String cardProtocol = "INNOVATRON_B_PRIME_CARD";
  final String ATR_HEX = "3B8F8001805A0A01032003111122334482900082";

  public StubCalypsoBPrime() {
    /* Get data */
    addHexCommand("FFCA 000000", "CA7195009000");
  }

  @Override
  public byte[] getATR() {
    return ByteArrayUtil.fromHex(ATR_HEX);
  }

  @Override
  public String getCardProtocol() {
    return cardProtocol;
  }
}
