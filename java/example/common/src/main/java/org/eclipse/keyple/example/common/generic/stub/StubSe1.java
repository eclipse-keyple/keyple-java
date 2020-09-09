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
import org.eclipse.keyple.plugin.stub.StubSecureElement;

/** Simple contact stub SE (no command) */
public class StubSe1 extends StubSecureElement {

  static final String seProtocol = "PROTOCOL_ISO7816_3";
  final String ATR_HEX = "3B3F9600805A0080C120000012345678829000"; // serial number : 12345678

  public StubSe1() {}

  @Override
  public byte[] getATR() {
    return ByteArrayUtil.fromHex(ATR_HEX);
  }

  @Override
  public String getSeProcotol() {
    return seProtocol;
  }
}
