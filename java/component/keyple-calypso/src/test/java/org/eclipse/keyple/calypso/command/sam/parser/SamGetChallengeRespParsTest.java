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
package org.eclipse.keyple.calypso.command.sam.parser;

import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.assertj.core.api.Java6Assertions.assertThat;

import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCommandException;
import org.eclipse.keyple.calypso.command.sam.parser.security.SamGetChallengeRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SamGetChallengeRespParsTest {
  private static final String SW1SW2_KO = "6700";
  private static final String SW1SW2_OK = "9000";
  private static final String SAM_CHALLENGE = "12345678";
  private static final String APDU_GET_CHALLENGE = SAM_CHALLENGE + SW1SW2_OK;

  @Test(expected = CalypsoSamCommandException.class)
  public void samGetChallengeRespPars_badStatus() {
    SamGetChallengeRespPars samGetChallengeRespPars =
        new SamGetChallengeRespPars(new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_KO), null), null);
    samGetChallengeRespPars.checkStatus();
    shouldHaveThrown(CalypsoSamCommandException.class);
  }

  @Test
  public void samGetChallengeRespPars_goodStatus_getSignature() {
    SamGetChallengeRespPars samGetChallengeRespPars =
        new SamGetChallengeRespPars(
            new ApduResponse(ByteArrayUtil.fromHex(APDU_GET_CHALLENGE), null), null);
    samGetChallengeRespPars.checkStatus();
    assertThat(samGetChallengeRespPars.getChallenge())
        .isEqualTo(ByteArrayUtil.fromHex(SAM_CHALLENGE));
  }
}
