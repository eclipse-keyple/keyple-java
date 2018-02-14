/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.utils;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.dto.*;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.parser.GetDataFciRespPars;
import org.keyple.calypso.commands.utils.ResponseUtils;

// @RunWith(MockitoJUnitRunner.class)
public class ResponseUtilsTest {

    // @InjectMocks
    // ResponseUtils responseUtils;

    // @Mock
    private byte[] apduResponse;

    private byte[] wrongApduResponse;

    private byte[] wrongApduResponseTwo;

    private byte[] apduResponseCaseTwo;

    private byte[] apduResponseCaseThree;

    private byte[] aid;

    private PoRevision revision;

    private PoRevision revision24;

    private byte[] transactionCounter;

    private byte[] randomNumber;

    private byte kif;

    // private KVC kvc;

    @Test
    public void TestToFCI() {

        // Case if
        apduResponse = new byte[] {(byte) 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52,
                0x2E, 0x49, 0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7,
                0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB7, 0x53, 0x07,
                0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01};

        aid = new byte[] {0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43, 0x41};
        // AID aidExpected = new AID(aid);
        byte[] fciProprietaryTemplate = new byte[] {(byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08,
                0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB7, 0x53, 0x07, 0x0A,
                0x3C, 0x11, 0x32, 0x14, 0x10, 0x01};
        byte[] fciIssuerDiscretionaryData =
                new byte[] {(byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A,
                        (byte) 0xB7, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01};
        byte[] applicationSN =
                new byte[] {0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB7};
        GetDataFciRespPars.StartupInformation startupInfoExpected =
                new GetDataFciRespPars.StartupInformation((byte) 0x0A, (byte) 0x3C, (byte) 0x11,
                        (byte) 0x32, (byte) 0x14, (byte) 0x10, (byte) 0x01);

        GetDataFciRespPars.FCI fciExpected = new GetDataFciRespPars.FCI(aid, fciProprietaryTemplate,
                fciIssuerDiscretionaryData, applicationSN, startupInfoExpected);
        GetDataFciRespPars.FCI fciTested = ResponseUtils.toFCI(apduResponse);

        Assert.assertArrayEquals(fciExpected.getApplicationSN(), fciTested.getApplicationSN());
        Assert.assertArrayEquals(fciExpected.getFciIssuerDiscretionaryData(),
                fciTested.getFciIssuerDiscretionaryData());
        Assert.assertArrayEquals(fciExpected.getFciProprietaryTemplate(),
                fciTested.getFciProprietaryTemplate());
        Assert.assertEquals(fciExpected.getStartupInformation().getApplicationSubtype(),
                fciTested.getStartupInformation().getApplicationSubtype());
        Assert.assertEquals(fciExpected.getStartupInformation().getApplicationType(),
                fciTested.getStartupInformation().getApplicationType());
        Assert.assertEquals(fciExpected.getStartupInformation().getBufferSize(),
                fciTested.getStartupInformation().getBufferSize());
        Assert.assertEquals(fciExpected.getStartupInformation().getPlatform(),
                fciTested.getStartupInformation().getPlatform());
        Assert.assertEquals(fciExpected.getStartupInformation().getSoftwareIssuer(),
                fciTested.getStartupInformation().getSoftwareIssuer());
        Assert.assertEquals(fciExpected.getStartupInformation().getSoftwareRevision(),
                fciTested.getStartupInformation().getSoftwareRevision());
        Assert.assertEquals(fciExpected.getStartupInformation().getSoftwareVersion(),
                fciTested.getStartupInformation().getSoftwareVersion());

        // Case else
        wrongApduResponse = new byte[] {(byte) 0x5F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54,
                0x52, 0x2E, 0x49, 0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13,
                (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB7,
                0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01};

        fciTested = ResponseUtils.toFCI(wrongApduResponse);

        Assert.assertNull(fciTested.getFciProprietaryTemplate());
        Assert.assertNull(fciTested.getFciIssuerDiscretionaryData());
        Assert.assertNull(fciTested.getApplicationSN());
        Assert.assertNull(fciTested.getStartupInformation());

        // Case if else
        wrongApduResponse = new byte[] {(byte) 0x6F, 0x22, (byte) 0x83, 0x08, 0x33, 0x4D, 0x54,
                0x52, 0x2E, 0x49, 0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13,
                (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB7,
                0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01};

        fciTested = ResponseUtils.toFCI(wrongApduResponse);

        Assert.assertNotNull(fciTested.getFciProprietaryTemplate());
        Assert.assertNotNull(fciTested.getFciIssuerDiscretionaryData());
        Assert.assertNotNull(fciTested.getApplicationSN());
        Assert.assertNotNull(fciTested.getStartupInformation());

        // Case if else 2
        wrongApduResponse = new byte[] {(byte) 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54,
                0x52, 0x2E, 0x49, 0x43, 0x41, (byte) 0xA6, 0x16, (byte) 0xBF, 0x0C, 0x13,
                (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB7,
                0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01};

        fciTested = ResponseUtils.toFCI(wrongApduResponse);

        Assert.assertNull(fciTested.getFciProprietaryTemplate());
        Assert.assertNotNull(fciTested.getFciIssuerDiscretionaryData());
        Assert.assertNotNull(fciTested.getApplicationSN());
        Assert.assertNotNull(fciTested.getStartupInformation());

        // Case if else 3
        wrongApduResponse = new byte[] {(byte) 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54,
                0x52, 0x2E, 0x49, 0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xAF, 0x0C, 0x13,
                (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB7,
                0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01};

        fciTested = ResponseUtils.toFCI(wrongApduResponse);

        Assert.assertNotNull(fciTested.getFciProprietaryTemplate());
        Assert.assertNull(fciTested.getFciIssuerDiscretionaryData());
        Assert.assertNotNull(fciTested.getApplicationSN());
        Assert.assertNotNull(fciTested.getStartupInformation());

        // Case if else 3bis
        wrongApduResponse = new byte[] {(byte) 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54,
                0x52, 0x2E, 0x49, 0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0D, 0x13,
                (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB7,
                0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01};

        fciTested = ResponseUtils.toFCI(wrongApduResponse);

        Assert.assertNotNull(fciTested.getFciProprietaryTemplate());
        Assert.assertNull(fciTested.getFciIssuerDiscretionaryData());
        Assert.assertNotNull(fciTested.getApplicationSN());
        Assert.assertNotNull(fciTested.getStartupInformation());

        // Case if else 4
        wrongApduResponse = new byte[] {(byte) 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54,
                0x52, 0x2E, 0x49, 0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13,
                (byte) 0xC8, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB7,
                0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01};

        fciTested = ResponseUtils.toFCI(wrongApduResponse);

        Assert.assertNotNull(fciTested.getFciProprietaryTemplate());
        Assert.assertNotNull(fciTested.getFciIssuerDiscretionaryData());
        Assert.assertNull(fciTested.getApplicationSN());
        Assert.assertNotNull(fciTested.getStartupInformation());

        // Case if else 4
        wrongApduResponse = new byte[] {(byte) 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54,
                0x52, 0x2E, 0x49, 0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13,
                (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB7,
                0x54, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01};

        fciTested = ResponseUtils.toFCI(wrongApduResponse);

        Assert.assertNotNull(fciTested.getFciProprietaryTemplate());
        Assert.assertNotNull(fciTested.getFciIssuerDiscretionaryData());
        Assert.assertNotNull(fciTested.getApplicationSN());
        Assert.assertNull(fciTested.getStartupInformation());

    }

    @Test
    public void TestToSecureSession() {
        apduResponse = new byte[] {(byte) 0x8F, 0x05, 0x75, 0x1A, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x30, 0x7E, (byte) 0x1D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00};

        transactionCounter = new byte[] {(byte) 0x8F, 0x05, 0x75};
        randomNumber = new byte[] {0x1A, 0x00, 0x00, 0x00, 0x00};
        kif = 0x00;
        byte kvc = (byte) 0x00;

        SecureSession.PoChallenge poChallengeExpected =
                new SecureSession.PoChallenge(transactionCounter, randomNumber);
        boolean isPreviousSessionRatifiedExpected = true;
        boolean isManageSecureSessionAuthorizedExpected = false;
        byte[] originalData = new byte[] {};

        SecureSession SecureSessionExpected = new SecureSession(poChallengeExpected,
                isPreviousSessionRatifiedExpected, isManageSecureSessionAuthorizedExpected, kif,
                kvc, originalData, apduResponse);
        SecureSession SecureSessionTested = ResponseUtils.toSecureSessionRev32(apduResponse);

        Assert.assertArrayEquals(SecureSessionExpected.getOriginalData(),
                SecureSessionTested.getOriginalData());
        Assert.assertArrayEquals(SecureSessionExpected.getSecureSessionData(),
                SecureSessionTested.getSecureSessionData());
        Assert.assertEquals(SecureSessionExpected.getKIF(), SecureSessionTested.getKIF());
        Assert.assertEquals(SecureSessionExpected.getKVC(), SecureSessionTested.getKVC());
        Assert.assertArrayEquals(SecureSessionExpected.getSessionChallenge().getRandomNumber(),
                SecureSessionTested.getSessionChallenge().getRandomNumber());
        Assert.assertArrayEquals(
                SecureSessionExpected.getSessionChallenge().getTransactionCounter(),
                SecureSessionTested.getSessionChallenge().getTransactionCounter());
    }

    @Test
    public void TestToSecureSessionRev2() {

        // Case Else
        apduResponse = new byte[] {(byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte) 0x14, (byte) 0x53};

        transactionCounter = new byte[] {(byte) 0x03, (byte) 0x0D, (byte) 0x14};
        randomNumber = new byte[] {(byte) 0x53};
        byte kvc = (byte) 0x7E;

        SecureSession.PoChallenge poChallengeExpected =
                new SecureSession.PoChallenge(transactionCounter, randomNumber);
        boolean isPreviousSessionRatifiedExpected = false;
        boolean isManageSecureSessionAuthorizedExpected = false;
        byte[] originalData = null;

        SecureSession SecureSessionExpected =
                new SecureSession(poChallengeExpected, isPreviousSessionRatifiedExpected,
                        isManageSecureSessionAuthorizedExpected, kvc, originalData, apduResponse);
        SecureSession SecureSessionTested = ResponseUtils.toSecureSessionRev2(apduResponse);

        Assert.assertArrayEquals(SecureSessionExpected.getSecureSessionData(),
                SecureSessionTested.getSecureSessionData());
        Assert.assertEquals(SecureSessionExpected.getKVC(), SecureSessionTested.getKVC());
        Assert.assertArrayEquals(SecureSessionExpected.getSessionChallenge().getRandomNumber(),
                SecureSessionTested.getSessionChallenge().getRandomNumber());
        Assert.assertArrayEquals(
                SecureSessionExpected.getSessionChallenge().getTransactionCounter(),
                SecureSessionTested.getSessionChallenge().getTransactionCounter());

        // Case If Else
        apduResponseCaseTwo = new byte[] {(byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte) 0x14,
                (byte) 0x53, (byte) 0x30, 0x00, 0x04, 0x01, 0x02, 0x03, 0x04};
        byte[] originalDataCaseTwo = new byte[] {(byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte) 0x14,
                (byte) 0x53, (byte) 0xFF, 0x00, 0x04, 0x01, 0x02, 0x03, 0x04};

        SecureSession SecureSessionExpectedCaseTwo = new SecureSession(poChallengeExpected,
                isPreviousSessionRatifiedExpected, isManageSecureSessionAuthorizedExpected, kvc,
                originalDataCaseTwo, apduResponseCaseTwo);
        SecureSession SecureSessionTestedCaseTwo =
                ResponseUtils.toSecureSessionRev2(apduResponseCaseTwo);

        Assert.assertArrayEquals(SecureSessionExpectedCaseTwo.getSecureSessionData(),
                SecureSessionTestedCaseTwo.getSecureSessionData());
        Assert.assertEquals(SecureSessionExpectedCaseTwo.getKVC(),
                SecureSessionTestedCaseTwo.getKVC());
        Assert.assertArrayEquals(
                SecureSessionExpectedCaseTwo.getSessionChallenge().getRandomNumber(),
                SecureSessionTestedCaseTwo.getSessionChallenge().getRandomNumber());
        Assert.assertArrayEquals(
                SecureSessionExpectedCaseTwo.getSessionChallenge().getTransactionCounter(),
                SecureSessionTestedCaseTwo.getSessionChallenge().getTransactionCounter());

        // Case If If
        apduResponseCaseThree = new byte[] {(byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte) 0x14,
                (byte) 0x53, (byte) 0xFF, 0x00, 0x04, 0x01, 0x02, 0x03, 0x04};
        byte[] originalDataCaseThree = new byte[] {(byte) 0x7E, (byte) 0x03, (byte) 0x0D,
                (byte) 0x14, (byte) 0x53, (byte) 0xFF, 0x00, 0x04, 0x01, 0x02, 0x03, 0x04};

        SecureSession SecureSessionExpectedCaseThree = new SecureSession(poChallengeExpected,
                isPreviousSessionRatifiedExpected, isManageSecureSessionAuthorizedExpected, kvc,
                originalDataCaseThree, apduResponseCaseThree);
        SecureSession SecureSessionTestedCaseThree =
                ResponseUtils.toSecureSessionRev2(apduResponseCaseThree);

        Assert.assertArrayEquals(SecureSessionExpectedCaseThree.getSecureSessionData(),
                SecureSessionTestedCaseThree.getSecureSessionData());
        Assert.assertEquals(SecureSessionExpectedCaseThree.getKVC(),
                SecureSessionTestedCaseThree.getKVC());
        Assert.assertArrayEquals(
                SecureSessionExpectedCaseThree.getSessionChallenge().getRandomNumber(),
                SecureSessionTestedCaseThree.getSessionChallenge().getRandomNumber());
        Assert.assertArrayEquals(
                SecureSessionExpectedCaseThree.getSessionChallenge().getTransactionCounter(),
                SecureSessionTestedCaseThree.getSessionChallenge().getTransactionCounter());
    }

    @Test
    public void TestToKVCRev2() {

        apduResponse = new byte[] {(byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte) 0x14, (byte) 0x53};
        byte KVCRev2Expected = (byte) 0x7E;
        byte KVCRev2Tested = ResponseUtils.toKVCRev2(apduResponse);

        Assert.assertEquals(KVCRev2Expected, KVCRev2Tested);

        apduResponseCaseTwo =
                new byte[] {(byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte) 0x14, (byte) 0x53};

    }

}
