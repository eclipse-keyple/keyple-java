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
package org.eclipse.keyple.calypso.command;

/**
 * Defines the two existing ISO7816 class bytes for a Calypso PO command.: LEGACY for REV1 / BPRIME
 * type PO, ISO for REV2/3 / B type
 *
 * @since 0.9
 */
public enum PoClass {

  /** Calypso revision 1 / B Prime protocol */
  LEGACY((byte) 0x94),
  /** Calypso revision 2 and higher */
  ISO((byte) 0x00);

  private final byte cla;

  /**
   * Gets the class byte.
   *
   * @return A byte
   * @since 0.9
   */
  public byte getValue() {
    return cla;
  }

  /**
   * Constructor
   *
   * @param cla class byte value
   */
  PoClass(byte cla) {
    this.cla = cla;
  }
}
