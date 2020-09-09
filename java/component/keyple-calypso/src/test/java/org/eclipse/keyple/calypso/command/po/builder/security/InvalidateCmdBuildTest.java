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
package org.eclipse.keyple.calypso.command.po.builder.security;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class InvalidateCmdBuildTest {
  private static final byte[] APDU_ISO_INVALIDATE = ByteArrayUtil.fromHex("0004000000");

  @Test
  public void invalidate() {
    InvalidateCmdBuild builder = new InvalidateCmdBuild(PoClass.ISO);
    byte[] apduRequestBytes = builder.getApduRequest().getBytes();
    assertThat(apduRequestBytes).isEqualTo(APDU_ISO_INVALIDATE);
  }
}
