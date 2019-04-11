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
package org.eclipse.keyple.calypso.util;


import org.eclipse.keyple.calypso.command.po.parser.security.AbstractOpenSessionRespPars;
import org.eclipse.keyple.calypso.command.po.parser.security.OpenSession24RespPars;
import org.eclipse.keyple.calypso.command.po.parser.security.OpenSession32RespPars;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResponseUtilsTest {
    @Test
    public void TestToSecureSession() {
        byte[] apduResponse = new byte[] {(byte) 0x8F, 0x05, 0x75, 0x1A, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x30, 0x7E, (byte) 0x1D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00};

        byte[] transactionCounter = new byte[] {(byte) 0x8F, 0x05, 0x75};
        byte[] randomNumber = new byte[] {0x1A, 0x00, 0x00, 0x00, 0x00};
        byte kif = 0x00;
        byte kvc = (byte) 0x00;

        boolean isPreviousSessionRatifiedExpected = true;
        boolean isManageSecureSessionAuthorizedExpected = false;
        byte[] originalData = new byte[0];

        AbstractOpenSessionRespPars.SecureSession SecureSessionExpected =
                new AbstractOpenSessionRespPars.SecureSession(transactionCounter, randomNumber,
                        isPreviousSessionRatifiedExpected, isManageSecureSessionAuthorizedExpected,
                        kif, kvc, originalData, apduResponse);
        AbstractOpenSessionRespPars.SecureSession SecureSessionTested =
                OpenSession32RespPars.createSecureSession(apduResponse);

        Assert.assertArrayEquals(SecureSessionExpected.getOriginalData(),
                SecureSessionTested.getOriginalData());
        Assert.assertArrayEquals(SecureSessionExpected.getSecureSessionData(),
                SecureSessionTested.getSecureSessionData());
        Assert.assertEquals(SecureSessionExpected.getKIF(), SecureSessionTested.getKIF());
        Assert.assertEquals(SecureSessionExpected.getKVC(), SecureSessionTested.getKVC());
        Assert.assertArrayEquals(SecureSessionExpected.getChallengeRandomNumber(),
                SecureSessionTested.getChallengeRandomNumber());
        Assert.assertArrayEquals(SecureSessionExpected.getChallengeTransactionCounter(),
                SecureSessionTested.getChallengeTransactionCounter());
    }

    @Test
    public void TestToSecureSessionRev2() {

        // Case Else
        byte[] apduResponse =
                new byte[] {(byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte) 0x14, (byte) 0x53};

        byte[] transactionCounter = new byte[] {(byte) 0x03, (byte) 0x0D, (byte) 0x14};
        byte[] randomNumber = new byte[] {(byte) 0x53};
        byte kvc = (byte) 0x7E;

        boolean isPreviousSessionRatifiedExpected = false;
        boolean isManageSecureSessionAuthorizedExpected = false;
        byte[] originalData = null;

        AbstractOpenSessionRespPars.SecureSession SecureSessionExpected =
                new AbstractOpenSessionRespPars.SecureSession(transactionCounter, randomNumber,
                        isPreviousSessionRatifiedExpected, isManageSecureSessionAuthorizedExpected,
                        kvc, originalData, apduResponse);
        AbstractOpenSessionRespPars.SecureSession SecureSessionTested =
                OpenSession24RespPars.createSecureSession(apduResponse);

        Assert.assertEquals(SecureSessionExpected.getSecureSessionData(),
                SecureSessionTested.getSecureSessionData());
        Assert.assertEquals(SecureSessionExpected.getKVC(), SecureSessionTested.getKVC());
        Assert.assertArrayEquals(SecureSessionExpected.getChallengeRandomNumber(),
                SecureSessionTested.getChallengeRandomNumber());
        Assert.assertArrayEquals(SecureSessionExpected.getChallengeTransactionCounter(),
                SecureSessionTested.getChallengeTransactionCounter());

        // Case If Else
        // byte[] apduResponseCaseTwo = new byte[] {(byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte)
        // 0x14,
        // (byte) 0x53, (byte) 0x30, 0x00, 0x04, 0x01, 0x02, 0x03, 0x04};
        // byte[] originalDataCaseTwo = new byte[] {(byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte)
        // 0x14,
        // (byte) 0x53, (byte) 0xFF, 0x00, 0x04, 0x01, 0x02, 0x03, 0x04};
        byte[] apduResponseCaseTwo = ByteArrayUtils.fromHex(
                "7E 030D1453 9999 00112233445566778899AABBCCDDEEFF 00112233445566778899AABBCC");
        byte[] originalDataCaseTwo = ByteArrayUtils.fromHex(
                "7E 030D1453 9999 00112233445566778899AABBCCDDEEFF 00112233445566778899AABBCC");

        AbstractOpenSessionRespPars.SecureSession SecureSessionExpectedCaseTwo =
                new AbstractOpenSessionRespPars.SecureSession(transactionCounter, randomNumber,
                        isPreviousSessionRatifiedExpected, isManageSecureSessionAuthorizedExpected,
                        kvc, originalDataCaseTwo, apduResponseCaseTwo);
        AbstractOpenSessionRespPars.SecureSession SecureSessionTestedCaseTwo =
                OpenSession24RespPars.createSecureSession(apduResponseCaseTwo);

        Assert.assertEquals(SecureSessionExpectedCaseTwo.getSecureSessionData(),
                SecureSessionTestedCaseTwo.getSecureSessionData());
        Assert.assertEquals(SecureSessionExpectedCaseTwo.getKVC(),
                SecureSessionTestedCaseTwo.getKVC());
        Assert.assertArrayEquals(SecureSessionExpectedCaseTwo.getChallengeRandomNumber(),
                SecureSessionTestedCaseTwo.getChallengeRandomNumber());
        Assert.assertArrayEquals(SecureSessionExpectedCaseTwo.getChallengeTransactionCounter(),
                SecureSessionTestedCaseTwo.getChallengeTransactionCounter());

        // Case If If
        // byte[] apduResponseCaseThree = new byte[] {(byte) 0x7E, (byte) 0x03, (byte) 0x0D,
        // (byte) 0x14, (byte) 0x53, (byte) 0xFF, 0x00, 0x04, 0x01, 0x02, 0x03, 0x04};
        // byte[] originalDataCaseThree = new byte[] {(byte) 0x7E, (byte) 0x03, (byte) 0x0D,
        // (byte) 0x14, (byte) 0x53, (byte) 0xFF, 0x00, 0x04, 0x01, 0x02, 0x03, 0x04};

        byte[] apduResponseCaseThree = ByteArrayUtils.fromHex(
                "7E 030D1453 9999 00112233445566778899AABBCCDDEEFF 00112233445566778899AABBCC");
        byte[] originalDataCaseThree = ByteArrayUtils.fromHex(
                "7E 030D1453 9999 00112233445566778899AABBCCDDEEFF 00112233445566778899AABBCC");

        AbstractOpenSessionRespPars.SecureSession SecureSessionExpectedCaseThree =
                new AbstractOpenSessionRespPars.SecureSession(transactionCounter, randomNumber,
                        isPreviousSessionRatifiedExpected, isManageSecureSessionAuthorizedExpected,
                        kvc, originalDataCaseThree, apduResponseCaseThree);
        AbstractOpenSessionRespPars.SecureSession SecureSessionTestedCaseThree =
                OpenSession24RespPars.createSecureSession(apduResponseCaseThree);

        Assert.assertEquals(SecureSessionExpectedCaseThree.getSecureSessionData(),
                SecureSessionTestedCaseThree.getSecureSessionData());
        Assert.assertEquals(SecureSessionExpectedCaseThree.getKVC(),
                SecureSessionTestedCaseThree.getKVC());
        Assert.assertArrayEquals(SecureSessionExpectedCaseThree.getChallengeRandomNumber(),
                SecureSessionTestedCaseThree.getChallengeRandomNumber());
        Assert.assertArrayEquals(SecureSessionExpectedCaseThree.getChallengeTransactionCounter(),
                SecureSessionTestedCaseThree.getChallengeTransactionCounter());
    }
}
