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

/** Simple contactless card Stub (no command) */
public class StubSmartCard2 extends StubSmartCard {

  static final String cardProtocol = "ISO_14443_4";
  final String ATR_HEX = "3B8E800180318066409089120802830190000B";

  public StubSmartCard2() {}

  @Override
  public byte[] getATR() {
    return ByteArrayUtil.fromHex(ATR_HEX);
  }

  @Override
  public String getCardProtocol() {
    return cardProtocol;
  }
}
