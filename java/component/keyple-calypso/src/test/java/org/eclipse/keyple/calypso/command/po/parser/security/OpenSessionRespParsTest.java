/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.po.parser.security;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.security.AbstractOpenSessionCmdBuild;
import org.eclipse.keyple.calypso.util.TestsUtilsResponseTabByteGenerator;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SelectionStatus;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OpenSessionRespParsTest {
    byte keyIndex = (byte) 0x01;
    byte[] terminalChallenge = ByteArrayUtil.fromHex("11223344");

    private void check(AbstractOpenSessionRespPars resp) {
        Assert.assertTrue(resp.isSuccessful());
    }

    @Test
    public void testgetResponse_rev2_4() {

        // expected response

        ApduResponse responseMockOpenSecureSession = new ApduResponse(ByteArrayUtil.fromHex(
                "CC 11223344 00112233445566778899AABBCCDDEEFF 00112233445566778899AABBCC 9000"),
                null);
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseMockOpenSecureSession);

        SeResponse responseMock = new SeResponse(true, true,
                new SelectionStatus(null, responseMockOpenSecureSession, true), apduResponses);
        ApduResponse response = responseMock.getApduResponses().get(0);

        AbstractOpenSessionCmdBuild openSessionCmdBuild = AbstractOpenSessionCmdBuild.create(
                PoRevision.REV2_4, keyIndex, terminalChallenge, (byte) 0x00, (byte) 0x00, "");
        AbstractOpenSessionRespPars abstractOpenSessionRespPars =
                (AbstractOpenSessionRespPars) openSessionCmdBuild.createResponseParser(response);
        check(abstractOpenSessionRespPars);
    }

    @Test
    public void testgetResponse_rev2_4_no_data() {

        // expected response

        ApduResponse responseMockOpenSecureSession =
                new ApduResponse(ByteArrayUtil.fromHex("CC 11223344 9000"), null);
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseMockOpenSecureSession);

        SeResponse responseMock = new SeResponse(true, true,
                new SelectionStatus(null, responseMockOpenSecureSession, true), apduResponses);
        ApduResponse response = responseMock.getApduResponses().get(0);

        AbstractOpenSessionCmdBuild openSessionCmdBuild = AbstractOpenSessionCmdBuild.create(
                PoRevision.REV2_4, keyIndex, terminalChallenge, (byte) 0x00, (byte) 0x00, "");
        AbstractOpenSessionRespPars abstractOpenSessionRespPars =
                (AbstractOpenSessionRespPars) openSessionCmdBuild.createResponseParser(response);
        check(abstractOpenSessionRespPars);
    }

    @Test
    public void testgetResponse_rev2_4_non_ratified() {

        // expected response

        ApduResponse responseMockOpenSecureSession = new ApduResponse(ByteArrayUtil.fromHex(
                "CC 11223344 9999 00112233445566778899AABBCCDDEEFF 00112233445566778899AABBCC 9000"),
                null);
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseMockOpenSecureSession);

        SeResponse responseMock = new SeResponse(true, true,
                new SelectionStatus(null, responseMockOpenSecureSession, true), apduResponses);
        ApduResponse response = responseMock.getApduResponses().get(0);

        AbstractOpenSessionCmdBuild openSessionCmdBuild = AbstractOpenSessionCmdBuild.create(
                PoRevision.REV2_4, keyIndex, terminalChallenge, (byte) 0x00, (byte) 0x00, "");
        AbstractOpenSessionRespPars abstractOpenSessionRespPars =
                (AbstractOpenSessionRespPars) openSessionCmdBuild.createResponseParser(response);
        check(abstractOpenSessionRespPars);
    }

    @Test
    public void testgetResponse_rev2_4_no_data_non_ratified() {

        // expected response

        ApduResponse responseMockOpenSecureSession =
                new ApduResponse(ByteArrayUtil.fromHex("CC 11223344 9999 9000"), null);
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseMockOpenSecureSession);

        SeResponse responseMock = new SeResponse(true, true,
                new SelectionStatus(null, responseMockOpenSecureSession, true), apduResponses);
        ApduResponse response = responseMock.getApduResponses().get(0);

        AbstractOpenSessionCmdBuild openSessionCmdBuild = AbstractOpenSessionCmdBuild.create(
                PoRevision.REV2_4, keyIndex, terminalChallenge, (byte) 0x00, (byte) 0x00, "");
        AbstractOpenSessionRespPars abstractOpenSessionRespPars =
                (AbstractOpenSessionRespPars) openSessionCmdBuild.createResponseParser(response);
        check(abstractOpenSessionRespPars);
    }

    @Test(expected = IllegalStateException.class)
    public void testgetResponse_rev2_4_bad_length_inf() {

        // expected response

        ApduResponse responseMockOpenSecureSession = new ApduResponse(ByteArrayUtil.fromHex(
                "CC 11223344 9999 00112233445566778899AABBCCDDEEFF 00112233445566778899AABBCCDDEEFF 9000"),
                null);
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseMockOpenSecureSession);

        SeResponse responseMock = new SeResponse(true, true,
                new SelectionStatus(null, responseMockOpenSecureSession, true), apduResponses);
        ApduResponse response = responseMock.getApduResponses().get(0);

        AbstractOpenSessionCmdBuild openSessionCmdBuild = AbstractOpenSessionCmdBuild.create(
                PoRevision.REV2_4, keyIndex, terminalChallenge, (byte) 0x00, (byte) 0x00, "");
        AbstractOpenSessionRespPars abstractOpenSessionRespPars =
                (AbstractOpenSessionRespPars) openSessionCmdBuild.createResponseParser(response);
        check(abstractOpenSessionRespPars);
    }

    @Test(expected = IllegalStateException.class)
    public void testgetResponse_rev2_4_bad_length_sup() {

        // expected response

        ApduResponse responseMockOpenSecureSession = new ApduResponse(
                ByteArrayUtil.fromHex("CC 11223344 9999 00112233445566778899AABBCCDDEEFF 9000"),
                null);
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseMockOpenSecureSession);

        SeResponse responseMock = new SeResponse(true, true,
                new SelectionStatus(null, responseMockOpenSecureSession, true), apduResponses);
        ApduResponse response = responseMock.getApduResponses().get(0);

        AbstractOpenSessionCmdBuild openSessionCmdBuild = AbstractOpenSessionCmdBuild.create(
                PoRevision.REV2_4, keyIndex, terminalChallenge, (byte) 0x00, (byte) 0x00, "");
        AbstractOpenSessionRespPars abstractOpenSessionRespPars =
                (AbstractOpenSessionRespPars) openSessionCmdBuild.createResponseParser(response);
        check(abstractOpenSessionRespPars);
    }

    @Test
    public void testgetResponse_rev3_1() {

        // expected response

        ApduResponse responseMockFci =
                TestsUtilsResponseTabByteGenerator.generateApduResponseValidRev3_1();
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseMockFci);

        SeResponse responseMock = new SeResponse(true, true,
                new SelectionStatus(null, responseMockFci, true), apduResponses);
        ApduResponse response = responseMock.getApduResponses().get(0);

        AbstractOpenSessionCmdBuild openSessionCmdBuild = AbstractOpenSessionCmdBuild.create(
                PoRevision.REV3_1, keyIndex, terminalChallenge, (byte) 0x00, (byte) 0x00, "");
        AbstractOpenSessionRespPars abstractOpenSessionRespPars =
                (AbstractOpenSessionRespPars) openSessionCmdBuild.createResponseParser(response);
        check(abstractOpenSessionRespPars);
    }

    @Test
    public void testgetResponse_rev3_2() {

        // expected response

        ApduResponse responseMockOS =
                TestsUtilsResponseTabByteGenerator.generateApduResponseValidRev3_2();
        ApduResponse responseMockFci =
                TestsUtilsResponseTabByteGenerator.generateApduResponseValidRev3_2();
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseMockOS);

        SeResponse responseMock = new SeResponse(true, true,
                new SelectionStatus(null, responseMockFci, true), apduResponses);
        ApduResponse response = responseMock.getApduResponses().get(0);

        AbstractOpenSessionCmdBuild openSessionCmdBuild = AbstractOpenSessionCmdBuild.create(
                PoRevision.REV3_2, keyIndex, terminalChallenge, (byte) 0x00, (byte) 0x00, "");
        AbstractOpenSessionRespPars abstractOpenSessionRespPars =
                (AbstractOpenSessionRespPars) openSessionCmdBuild.createResponseParser(response);
        check(abstractOpenSessionRespPars);
    }

}
