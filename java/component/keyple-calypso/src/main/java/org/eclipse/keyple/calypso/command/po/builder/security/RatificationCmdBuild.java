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
package org.eclipse.keyple.calypso.command.po.builder.security;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.core.reader.message.ApduRequest;

/**
 * The RatificationCmdBuild class provides the ApduRequest dedicated to the ratification command,
 * i.e. the command sent after closing the secure session to handle the ratification mechanism. <br>
 * This particular builder is not associated with any parser since the response to this command is
 * always an error and is never checked.
 */
public final class RatificationCmdBuild {
  private RatificationCmdBuild() {}

  /**
   * @param poClass the PO class
   * @return the ApduRequest ratification command according to the PO class provided
   */
  public static ApduRequest getApduRequest(PoClass poClass) {
    byte[] ratificationApdu =
        new byte[] {poClass.getValue(), (byte) 0xB2, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    return new ApduRequest(ratificationApdu, false);
  }
}
