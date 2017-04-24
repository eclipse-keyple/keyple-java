package cna.sdk.calypso.commandsSet;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import cna.sdk.calypso.commandset.ResponseUtils;
import cna.sdk.calypso.commandset.enumSFI;
import cna.sdk.calypso.commandset.dto.AID;
import cna.sdk.calypso.commandset.dto.EF;
import cna.sdk.calypso.commandset.dto.FCI;
import cna.sdk.calypso.commandset.dto.KIF;
import cna.sdk.calypso.commandset.dto.KVC;
import cna.sdk.calypso.commandset.dto.POChallenge;
import cna.sdk.calypso.commandset.dto.POHalfSessionSignature;
import cna.sdk.calypso.commandset.dto.PostponedData;
import cna.sdk.calypso.commandset.dto.Ratification;
import cna.sdk.calypso.commandset.dto.SamChallenge;
import cna.sdk.calypso.commandset.dto.SamHalfSessionSignature;
import cna.sdk.calypso.commandset.dto.SecureSession;
import cna.sdk.calypso.commandset.dto.StartupInformation;
import cna.sdk.calypso.commandset.dto.TransactionCounter;
import cna.sdk.calypso.commandset.po.PoRevision;

@RunWith(BlockJUnit4ClassRunner.class)
public class ResponseUtilsTest {

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

    private KIF kif;

    private KVC kvc;

    @Test
    public void TestToAID() {

        // Case if
        apduResponse = new byte[] { 0x4F, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43, 0x41 };
        aid = new byte[] { 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43, 0x41 };

        AID aidExpected = new AID(aid);
        AID aidTested = ResponseUtils.toAID(apduResponse);

        Assert.assertArrayEquals(aidExpected.getValue(), aidTested.getValue());

        // Case else
        wrongApduResponse = new byte[] { 0x3F, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43, 0x41 };
        aidTested = ResponseUtils.toAID(wrongApduResponse);

        Assert.assertNull(aidTested);
    }

    @Test
    public void TestToEFList() {

        // Case if
        apduResponse = new byte[] { (byte) 0xC0, 0x20, (byte) 0xC1, 0x06, 0x3F, 0x04, 0x09, 0x02, 0x10, 0x01,
                (byte) 0xC1, 0x06, 0x00, 0x03, 0x07, 0x02, 0x1D, 0x01, (byte) 0xC1, 0x06, 0x2F, 0x10, 0x08, 0x02, 0x1D,
                0x02, (byte) 0xC1, 0x06, 0x00, 0x02, 0x1D, 0x02, 0x1D, 0x01, };

        EF efFileOne = new EF((new byte[] { 0x3F, 0x04 }), enumSFI.CONTRACT_FILE, (byte) 0x02, (byte) 0x10,
                (byte) 0x01);
        EF efFileTwo = new EF((new byte[] { 0x00, 0x03 }), enumSFI.ENVIRONMENT_FILE, (byte) 0x02, (byte) 0x1D,
                (byte) 0x01);
        EF efFileThree = new EF((new byte[] { 0x2F, 0x10 }), enumSFI.EVENT_LOG_FILE, (byte) 0x02, (byte) 0x1D,
                (byte) 0x02);
        EF efFileFour = new EF((new byte[] { 0x00, 0x02 }), enumSFI.SPECIAL_EVENT_FILE, (byte) 0x02, (byte) 0x1D,
                (byte) 0x01);
        List<EF> listOfEfFile = new ArrayList<>();
        listOfEfFile.add(efFileOne);
        listOfEfFile.add(efFileTwo);
        listOfEfFile.add(efFileThree);
        listOfEfFile.add(efFileFour);

        List<EF> listOfEfFileExpected = new ArrayList<>(listOfEfFile);
        List<EF> listOfEfFileTested = ResponseUtils.toEFList(apduResponse);

        for (int i = 0; i < listOfEfFileExpected.size(); i++) {
            Assert.assertArrayEquals(listOfEfFileExpected.get(i).getLid(), listOfEfFileTested.get(i).getLid());
            Assert.assertEquals(listOfEfFileExpected.get(i).getFileType(), listOfEfFileTested.get(i).getFileType());
            Assert.assertEquals(listOfEfFileExpected.get(i).getNumberRec(), listOfEfFileTested.get(i).getNumberRec());
            Assert.assertEquals(listOfEfFileExpected.get(i).getRecSize(), listOfEfFileTested.get(i).getRecSize());
            Assert.assertEquals(listOfEfFileExpected.get(i).getSfi(), listOfEfFileTested.get(i).getSfi());
        }

        // Case else
        wrongApduResponse = new byte[] { (byte) 0xC2, 0x20, (byte) 0xC1, 0x06, 0x3F, 0x04, 0x04, 0x02, 0x10, 0x01,
                (byte) 0xC1, 0x06, 0x00, 0x03, 0x03, 0x02, 0x1D, 0x01, (byte) 0xC1, 0x06, 0x2F, 0x10, 0x05, 0x02, 0x1D,
                0x02, (byte) 0xC1, 0x06, 0x00, 0x02, 0x02, 0x02, 0x1D, 0x01, };

        listOfEfFileTested = ResponseUtils.toEFList(wrongApduResponse);

        Assert.assertNotEquals(listOfEfFileTested.size(), listOfEfFileExpected.size());
    }

    @Test
    public void TestToFCI() {

        // Case if
        apduResponse = new byte[] { (byte) 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43,
                0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A,
                (byte) 0x9A, (byte) 0xB7, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01 };

        aid = new byte[] { 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43, 0x41 };
        AID aidExpected = new AID(aid);
        byte[] fciProprietaryTemplate = new byte[] { (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00,
                0x27, 0x4A, (byte) 0x9A, (byte) 0xB7, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01 };
        byte[] fciIssuerDiscretionaryData = new byte[] { (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A,
                (byte) 0x9A, (byte) 0xB7, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01 };
        byte[] applicationSN = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB7 };
        StartupInformation startupInfoExpected = new StartupInformation((byte) 0x0A, (byte) 0x3C, (byte) 0x11,
                (byte) 0x32, (byte) 0x14, (byte) 0x10, (byte) 0x01);

        FCI fciExpected = new FCI(aidExpected, fciProprietaryTemplate, fciIssuerDiscretionaryData, applicationSN,
                startupInfoExpected);
        FCI fciTested = ResponseUtils.toFCI(apduResponse);

        Assert.assertArrayEquals(fciExpected.getAid().getValue(), fciTested.getAid().getValue());
        Assert.assertArrayEquals(fciExpected.getApplicationSN(), fciTested.getApplicationSN());
        Assert.assertArrayEquals(fciExpected.getFciIssuerDiscretionaryData(),
                fciTested.getFciIssuerDiscretionaryData());
        Assert.assertArrayEquals(fciExpected.getFciProprietaryTemplate(), fciTested.getFciProprietaryTemplate());
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
        wrongApduResponse = new byte[] { (byte) 0x5F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43,
                0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A,
                (byte) 0x9A, (byte) 0xB7, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01 };

        fciTested = ResponseUtils.toFCI(wrongApduResponse);

        Assert.assertNull(fciTested.getAid());
        Assert.assertNull(fciTested.getFciProprietaryTemplate());
        Assert.assertNull(fciTested.getFciIssuerDiscretionaryData());
        Assert.assertNull(fciTested.getApplicationSN());
        Assert.assertNull(fciTested.getStartupInformation());

        // Case if else
        wrongApduResponse = new byte[] { (byte) 0x6F, 0x22, (byte) 0x83, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43,
                0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A,
                (byte) 0x9A, (byte) 0xB7, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01 };

        fciTested = ResponseUtils.toFCI(wrongApduResponse);

        Assert.assertNull(fciTested.getAid());
        Assert.assertNotNull(fciTested.getFciProprietaryTemplate());
        Assert.assertNotNull(fciTested.getFciIssuerDiscretionaryData());
        Assert.assertNotNull(fciTested.getApplicationSN());
        Assert.assertNotNull(fciTested.getStartupInformation());

        // Case if else 2
        wrongApduResponse = new byte[] { (byte) 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43,
                0x41, (byte) 0xA6, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A,
                (byte) 0x9A, (byte) 0xB7, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01 };

        fciTested = ResponseUtils.toFCI(wrongApduResponse);

        Assert.assertNotNull(fciTested.getAid());
        Assert.assertNull(fciTested.getFciProprietaryTemplate());
        Assert.assertNotNull(fciTested.getFciIssuerDiscretionaryData());
        Assert.assertNotNull(fciTested.getApplicationSN());
        Assert.assertNotNull(fciTested.getStartupInformation());

        // Case if else 3
        wrongApduResponse = new byte[] { (byte) 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43,
                0x41, (byte) 0xA5, 0x16, (byte) 0xAF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A,
                (byte) 0x9A, (byte) 0xB7, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01 };

        fciTested = ResponseUtils.toFCI(wrongApduResponse);

        Assert.assertNotNull(fciTested.getAid());
        Assert.assertNotNull(fciTested.getFciProprietaryTemplate());
        Assert.assertNull(fciTested.getFciIssuerDiscretionaryData());
        Assert.assertNotNull(fciTested.getApplicationSN());
        Assert.assertNotNull(fciTested.getStartupInformation());

        // Case if else 3bis
        wrongApduResponse = new byte[] { (byte) 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43,
                0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0D, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A,
                (byte) 0x9A, (byte) 0xB7, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01 };

        fciTested = ResponseUtils.toFCI(wrongApduResponse);

        Assert.assertNotNull(fciTested.getAid());
        Assert.assertNotNull(fciTested.getFciProprietaryTemplate());
        Assert.assertNull(fciTested.getFciIssuerDiscretionaryData());
        Assert.assertNotNull(fciTested.getApplicationSN());
        Assert.assertNotNull(fciTested.getStartupInformation());

        // Case if else 4
        wrongApduResponse = new byte[] { (byte) 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43,
                0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC8, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A,
                (byte) 0x9A, (byte) 0xB7, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01 };

        fciTested = ResponseUtils.toFCI(wrongApduResponse);

        Assert.assertNotNull(fciTested.getAid());
        Assert.assertNotNull(fciTested.getFciProprietaryTemplate());
        Assert.assertNotNull(fciTested.getFciIssuerDiscretionaryData());
        Assert.assertNull(fciTested.getApplicationSN());
        Assert.assertNotNull(fciTested.getStartupInformation());

        // Case if else 4
        wrongApduResponse = new byte[] { (byte) 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43,
                0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A,
                (byte) 0x9A, (byte) 0xB7, 0x54, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01 };

        fciTested = ResponseUtils.toFCI(wrongApduResponse);

        Assert.assertNotNull(fciTested.getAid());
        Assert.assertNotNull(fciTested.getFciProprietaryTemplate());
        Assert.assertNotNull(fciTested.getFciIssuerDiscretionaryData());
        Assert.assertNotNull(fciTested.getApplicationSN());
        Assert.assertNull(fciTested.getStartupInformation());

    }

    @Test
    public void TestToSamChallenge() {
        apduResponse = new byte[] { 0x03, 0x0E, 0x01, 0x0F };

        transactionCounter = new byte[] { 0x03, 0x0E, 0x01 };
        randomNumber = new byte[] { 0x0F };

        SamChallenge samChallengeExpected = new SamChallenge(transactionCounter, randomNumber);
        SamChallenge samChallengeTested = ResponseUtils.toSamChallenge(apduResponse);

        Assert.assertArrayEquals(samChallengeExpected.getRandomNumber(), samChallengeTested.getRandomNumber());
        Assert.assertArrayEquals(samChallengeExpected.getTransactionCounter(),
                samChallengeTested.getTransactionCounter());
    }

    @Test
    public void TestToSecureSession() {
        apduResponse = new byte[] { (byte) 0x8F, 0x05, 0x75, 0x1A, 0x00, 0x30, 0x7E, (byte) 0x1D, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        transactionCounter = new byte[] { (byte) 0x8F, 0x05, 0x75 };
        randomNumber = new byte[] { 0x1A };
        kif = new KIF((byte) 0x30);
        kvc = new KVC((byte) 0x7E);

        POChallenge poChallengeExpected = new POChallenge(transactionCounter, randomNumber);
        boolean isPreviousSessionRatifiedExpected = true;
        boolean isManageSecureSessionAuthorizedExpected = true;
        byte[] originalData = new byte[] { (byte) 0x8F, 0x05, 0x75, 0x1A, 0x00, 0x30, 0x7E, (byte) 0x1D, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        SecureSession SecureSessionExpected = new SecureSession(poChallengeExpected, isPreviousSessionRatifiedExpected,
                isManageSecureSessionAuthorizedExpected, kif, kvc, originalData, apduResponse);
        SecureSession SecureSessionTested = ResponseUtils.toSecureSession(apduResponse);

        Assert.assertArrayEquals(SecureSessionExpected.getOriginalData(), SecureSessionTested.getOriginalData());
        Assert.assertArrayEquals(SecureSessionExpected.getSecureSessionData(),
                SecureSessionTested.getSecureSessionData());
        Assert.assertEquals(SecureSessionExpected.getKIF().getValue(), SecureSessionTested.getKIF().getValue());
        Assert.assertEquals(SecureSessionExpected.getKVC().getValue(), SecureSessionTested.getKVC().getValue());
        Assert.assertArrayEquals(SecureSessionExpected.getSessionChallenge().getRandomNumber(),
                SecureSessionTested.getSessionChallenge().getRandomNumber());
        Assert.assertArrayEquals(SecureSessionExpected.getSessionChallenge().getTransactionCounter(),
                SecureSessionTested.getSessionChallenge().getTransactionCounter());
    }

    @Test
    public void TestToSecureSessionRev2() {

        // Case Else
        apduResponse = new byte[] { (byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte) 0x14, (byte) 0x53 };

        transactionCounter = new byte[] { (byte) 0x03, (byte) 0x0D, (byte) 0x14 };
        randomNumber = new byte[] { (byte) 0x53 };
        kvc = new KVC((byte) 0x7E);

        POChallenge poChallengeExpected = new POChallenge(transactionCounter, randomNumber);
        boolean isPreviousSessionRatifiedExpected = true;
        boolean isManageSecureSessionAuthorizedExpected = true;
        byte[] originalData = new byte[] { (byte) 0x7E, 0x03, 0x0D, 0x14, 0x53 };

        SecureSession SecureSessionExpected = new SecureSession(poChallengeExpected, isPreviousSessionRatifiedExpected,
                isManageSecureSessionAuthorizedExpected, kvc, originalData, apduResponse);
        SecureSession SecureSessionTested = ResponseUtils.toSecureSessionRev2(apduResponse);

        Assert.assertArrayEquals(SecureSessionExpected.getSecureSessionData(),
                SecureSessionTested.getSecureSessionData());
        Assert.assertEquals(SecureSessionExpected.getKVC().getValue(), SecureSessionTested.getKVC().getValue());
        Assert.assertArrayEquals(SecureSessionExpected.getSessionChallenge().getRandomNumber(),
                SecureSessionTested.getSessionChallenge().getRandomNumber());
        Assert.assertArrayEquals(SecureSessionExpected.getSessionChallenge().getTransactionCounter(),
                SecureSessionTested.getSessionChallenge().getTransactionCounter());

        // Case If Else
        apduResponseCaseTwo = new byte[] { (byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte) 0x14, (byte) 0x53, (byte) 0x30,
                0x00, 0x04, 0x01, 0x02, 0x03, 0x04 };
        byte[] originalDataCaseTwo = new byte[] { (byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte) 0x14, (byte) 0x53,
                (byte) 0xFF, 0x00, 0x04, 0x01, 0x02, 0x03, 0x04 };

        SecureSession SecureSessionExpectedCaseTwo = new SecureSession(poChallengeExpected,
                isPreviousSessionRatifiedExpected, isManageSecureSessionAuthorizedExpected, kvc, originalDataCaseTwo,
                apduResponseCaseTwo);
        SecureSession SecureSessionTestedCaseTwo = ResponseUtils.toSecureSessionRev2(apduResponseCaseTwo);

        Assert.assertArrayEquals(SecureSessionExpectedCaseTwo.getSecureSessionData(),
                SecureSessionTestedCaseTwo.getSecureSessionData());
        Assert.assertEquals(SecureSessionExpectedCaseTwo.getKVC().getValue(),
                SecureSessionTestedCaseTwo.getKVC().getValue());
        Assert.assertArrayEquals(SecureSessionExpectedCaseTwo.getSessionChallenge().getRandomNumber(),
                SecureSessionTestedCaseTwo.getSessionChallenge().getRandomNumber());
        Assert.assertArrayEquals(SecureSessionExpectedCaseTwo.getSessionChallenge().getTransactionCounter(),
                SecureSessionTestedCaseTwo.getSessionChallenge().getTransactionCounter());

        // Case If If
        apduResponseCaseThree = new byte[] { (byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte) 0x14, (byte) 0x53,
                (byte) 0xFF, 0x00, 0x04, 0x01, 0x02, 0x03, 0x04 };
        byte[] originalDataCaseThree = new byte[] { (byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte) 0x14, (byte) 0x53,
                (byte) 0xFF, 0x00, 0x04, 0x01, 0x02, 0x03, 0x04 };

        SecureSession SecureSessionExpectedCaseThree = new SecureSession(poChallengeExpected,
                isPreviousSessionRatifiedExpected, isManageSecureSessionAuthorizedExpected, kvc, originalDataCaseThree,
                apduResponseCaseThree);
        SecureSession SecureSessionTestedCaseThree = ResponseUtils.toSecureSessionRev2(apduResponseCaseThree);

        Assert.assertArrayEquals(SecureSessionExpectedCaseThree.getSecureSessionData(),
                SecureSessionTestedCaseThree.getSecureSessionData());
        Assert.assertEquals(SecureSessionExpectedCaseThree.getKVC().getValue(),
                SecureSessionTestedCaseThree.getKVC().getValue());
        Assert.assertArrayEquals(SecureSessionExpectedCaseThree.getSessionChallenge().getRandomNumber(),
                SecureSessionTestedCaseThree.getSessionChallenge().getRandomNumber());
        Assert.assertArrayEquals(SecureSessionExpectedCaseThree.getSessionChallenge().getTransactionCounter(),
                SecureSessionTestedCaseThree.getSessionChallenge().getTransactionCounter());
    }

    @Test
    public void TestToSamHalfSessionSignature() {

        apduResponse = new byte[] { (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E };

        byte[] sessionSignature = new byte[] { (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E };

        SamHalfSessionSignature samHalfSessionSignatureExpected = new SamHalfSessionSignature(sessionSignature);
        SamHalfSessionSignature samHalfSessionSignatureTested = ResponseUtils.toSamHalfSessionSignature(apduResponse);

        Assert.assertArrayEquals(samHalfSessionSignatureExpected.getValue(), samHalfSessionSignatureTested.getValue());

    }

    @Test
    public void TestToPOHalfSessionSignature() {

        apduResponse = new byte[] { (byte) 0x4D, (byte) 0xBD, (byte) 0xC9, 0x60 };
        apduResponseCaseTwo = new byte[] { (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, (byte) 0xA7, 0x21, (byte) 0xC2, 0x2E };
        apduResponseCaseThree = new byte[] { (byte) 0xA8, 0x31, (byte) 0xC3 };

        byte[] sessionSignature = new byte[] { (byte) 0x4D, (byte) 0xBD, (byte) 0xC9, 0x60 };
        byte[] sessionSignatureCaseTwo = new byte[] { (byte) 0xA7, 0x21, (byte) 0xC2, 0x2E };

        // Case Length = 4
        POHalfSessionSignature poHalfSessionSignatureExpected = new POHalfSessionSignature(sessionSignature);
        POHalfSessionSignature poHalfSessionSignatureTested = ResponseUtils.toPoHalfSessionSignature(apduResponse);

        Assert.assertArrayEquals(poHalfSessionSignatureExpected.getValue(), poHalfSessionSignatureTested.getValue());

        // Case Length = 8
        POHalfSessionSignature poHalfSessionSignatureExpectedCaseTwo = new POHalfSessionSignature(
                sessionSignatureCaseTwo);
        POHalfSessionSignature poHalfSessionSignatureTestedCaseTwo = ResponseUtils
                .toPoHalfSessionSignature(apduResponseCaseTwo);

        Assert.assertArrayEquals(poHalfSessionSignatureExpectedCaseTwo.getValue(),
                poHalfSessionSignatureTestedCaseTwo.getValue());

        // Case Other
        POHalfSessionSignature poHalfSessionSignatureTestedCaseThree = ResponseUtils
                .toPoHalfSessionSignature(apduResponseCaseThree);
        Assert.assertNull(poHalfSessionSignatureTestedCaseThree.getValue());

    }

    @Test
    public void TestToKVC() {

        // Case Else
        apduResponse = new byte[] { (byte) 0x8F, 0x05, 0x75, 0x1A, 0x00, 0x30, 0x7E, (byte) 0x1D, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        KVC KVCExpected = new KVC((byte) 0x7E);
        KVC KVCTested = ResponseUtils.toKVC(apduResponse);

        Assert.assertEquals(KVCExpected.getValue(), KVCTested.getValue());

        // Case If
        wrongApduResponse = new byte[] { (byte) 0x8F, 0x05, 0x75 };
        KVC KVCTestedTwo = ResponseUtils.toKVC(wrongApduResponse);

        Assert.assertNull(KVCTestedTwo);

    }

    @Test
    public void TestToKVCRev2() {

        apduResponse = new byte[] { (byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte) 0x14, (byte) 0x53 };
        KVC KVCRev2Expected = new KVC((byte) 0x7E);
        KVC KVCRev2Tested = ResponseUtils.toKVCRev2(apduResponse);

        Assert.assertEquals(KVCRev2Expected.getValue(), KVCRev2Tested.getValue());

        apduResponseCaseTwo = new byte[] { (byte) 0x7E, (byte) 0x03, (byte) 0x0D, (byte) 0x14, (byte) 0x53 };

    }

    @Test
    public void TestToKIF() {

        // Case Else
        apduResponse = new byte[] { (byte) 0x8F, 0x05, 0x75, 0x1A, 0x00, 0x30, 0x7E, (byte) 0x1D, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        KIF KIFExpected = new KIF((byte) 0x30);
        KIF KIFTested = ResponseUtils.toKIF(apduResponse);

        Assert.assertEquals(KIFExpected.getValue(), KIFTested.getValue());

        // Case If
        wrongApduResponse = new byte[] { (byte) 0x8F, 0x05, 0x75, 0x1A, 0x00, (byte) 0xFF, 0x7E, (byte) 0x1D, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        KIF KIFTestedTwo = ResponseUtils.toKIF(wrongApduResponse);

        Assert.assertEquals(KIFExpected.getValue(), KIFTestedTwo.getValue());

    }

    @Test
    public void TestIsPreviousSessionRatified() {

        apduResponse = new byte[] { (byte) 0x8F, 0x05, 0x75, 0x1A, 0x00, 0x30, 0x7E, (byte) 0x1D, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        wrongApduResponse = new byte[] { (byte) 0x8F, 0x05, 0x75, 0x1A, 0x00, 0x01, 0x7E, (byte) 0x1D, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        revision = PoRevision.REV3_1;

        Ratification isNotPreviousSessionRatifiedExpected = new Ratification(false);
        Ratification isPreviousSessionRatifiedExpected = new Ratification(true);

        // Case Rev 3.1 - Ratified

        Ratification isPreviousSessionRatifiedTested31 = ResponseUtils.isPreviousSessionRatified(apduResponse,
                revision);
        Assert.assertEquals(isPreviousSessionRatifiedExpected.isRatified(),
                isPreviousSessionRatifiedTested31.isRatified());

        // Case Rev 3.1 - Not Ratified

        Ratification isNotPreviousSessionRatifiedTested31 = ResponseUtils.isPreviousSessionRatified(wrongApduResponse,
                revision);
        Assert.assertEquals(isNotPreviousSessionRatifiedExpected.isRatified(),
                isNotPreviousSessionRatifiedTested31.isRatified());

        // Case Rev 3.2 - Ratified
        revision = PoRevision.REV3_2;

        Ratification isPreviousSessionRatifiedTested32 = ResponseUtils.isPreviousSessionRatified(apduResponse,
                revision);
        Assert.assertEquals(isPreviousSessionRatifiedExpected.isRatified(),
                isPreviousSessionRatifiedTested32.isRatified());

        // Case Rev 3.2 - Not Ratified
        Ratification isNotPreviousSessionRatifiedTested32 = ResponseUtils.isPreviousSessionRatified(wrongApduResponse,
                revision);
        Assert.assertEquals(isNotPreviousSessionRatifiedExpected.isRatified(),
                isNotPreviousSessionRatifiedTested32.isRatified());

        // Case Rev 2.4 - Not Ratified
        revision24 = PoRevision.REV2_4;

        Ratification isNotPreviousSessionRatifiedTested24 = ResponseUtils.isPreviousSessionRatified(apduResponse,
                revision24);
        Assert.assertEquals(isNotPreviousSessionRatifiedExpected.isRatified(),
                isNotPreviousSessionRatifiedTested24.isRatified());

        // Case Rev 2.4 - Ratified
        wrongApduResponseTwo = new byte[] { (byte) 0x8F, 0x05, 0x75, 0x1A, 0x00 };

        Ratification isPreviousSessionRatifiedTested24 = ResponseUtils.isPreviousSessionRatified(wrongApduResponseTwo,
                revision24);
        Assert.assertEquals(isPreviousSessionRatifiedExpected.isRatified(),
                isPreviousSessionRatifiedTested24.isRatified());

    }

    @Test
    public void TestToTransactionCounter() {

        apduResponse = new byte[] { (byte) 0x8F, 0x05, 0x75, 0x1A, 0x00, 0x30, 0x7E, (byte) 0x1D, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        byte B1 = (byte) 0x05;
        byte B2 = (byte) 0x75;
        byte B3 = (byte) 0x1A;
        byte B4 = (byte) 0x00;

        int counter = B1 + B2 + B3;
        int counter24 = B2 + B3 + B4;
        int counterUnkonwn = 0;

        TransactionCounter transactionCounterExpected = new TransactionCounter(counter);
        TransactionCounter transactionCounterExpected24 = new TransactionCounter(counter24);

        // Case Revision 3.1
        revision = PoRevision.REV3_1;
        TransactionCounter transactionCounterTested32 = ResponseUtils.toTransactionCounter(apduResponse, revision);
        Assert.assertEquals(transactionCounterExpected.getValue(), transactionCounterTested32.getValue());

        // Case Revision 3.2
        revision = PoRevision.REV3_2;
        TransactionCounter transactionCounterTested31 = ResponseUtils.toTransactionCounter(apduResponse, revision);
        Assert.assertEquals(transactionCounterExpected.getValue(), transactionCounterTested31.getValue());

        // Case Revision 2.4
        revision24 = PoRevision.REV2_4;
        TransactionCounter transactionCounterTested24 = ResponseUtils.toTransactionCounter(apduResponse, revision24);
        Assert.assertEquals(transactionCounterExpected24.getValue(), transactionCounterTested24.getValue());

    }

    @Test
    public void TestToRecordData() {

        apduResponse = new byte[] { (byte) 0x8F, 0x05, 0x75, 0x1A, 0x00, 0x30, 0x7E, (byte) 0x1D, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        revision = PoRevision.REV3_1;

        byte B1 = (byte) 0x05;
        byte B2 = (byte) 0x75;
        byte B3 = (byte) 0x1A;

        int counter = B1 + B2 + B3;

        TransactionCounter transactionCounterExpected = new TransactionCounter(counter);
        TransactionCounter transactionCounterTested = ResponseUtils.toTransactionCounter(apduResponse, revision);

        Assert.assertEquals(transactionCounterExpected.getValue(), transactionCounterTested.getValue());

    }

    @Test
    public void TestToPostponedData() {

        apduResponse = new byte[] { 0x04, 0x05, 0x75, 0x1A, 0x00, 0x30, 0x7E, (byte) 0x1D };
        apduResponseCaseTwo = new byte[] { 0x04, 0x05, 0x75, 0x1A };
        apduResponseCaseThree = new byte[0];

        boolean hasPostPonedDataTrue = true;
        boolean hasPostPonedDataFalse = false;
        byte[] postponedData = new byte[] { 0x05, 0x75, 0x1A };

        // Case If
        PostponedData postPonedDataExpected = new PostponedData(hasPostPonedDataTrue, postponedData);
        PostponedData postPonedDataTested = ResponseUtils.toPostponedData(apduResponse);

        Assert.assertArrayEquals(postPonedDataExpected.getPostponedData(), postPonedDataTested.getPostponedData());
        Assert.assertEquals(postPonedDataExpected.getHasPostponedData(), postPonedDataTested.getHasPostponedData());

        // Case Else if
        PostponedData postPonedDataExpectedTwoThree = new PostponedData(hasPostPonedDataFalse, postponedData);

        PostponedData postPonedDataTestedTwo = ResponseUtils.toPostponedData(apduResponseCaseTwo);
        PostponedData postPonedDataTestedThree = ResponseUtils.toPostponedData(apduResponseCaseThree);

        Assert.assertNull(postPonedDataTestedTwo.getPostponedData());
        Assert.assertEquals(postPonedDataExpectedTwoThree.getHasPostponedData(),
                postPonedDataTestedTwo.getHasPostponedData());

        Assert.assertNull(postPonedDataTestedThree.getPostponedData());
        Assert.assertEquals(postPonedDataExpectedTwoThree.getHasPostponedData(),
                postPonedDataTestedThree.getHasPostponedData());

    }
}
