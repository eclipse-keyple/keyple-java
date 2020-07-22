/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.po.parser.storedvalue;

import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/*
 * In addition to the usual checks, these tests also aim to verify binary arithmetic around the
 * balance and amount fields.
 */
public class SvGetRespParsTest {
    byte[] header;

    // init before each test
    @Before
    public void SetUp() {
        header = ByteArrayUtil.fromHex("7C000721");
    }

    @Test(expected = IllegalStateException.class)
    public void badLength() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("0011 9000"), null);
        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);
    }

    @Test
    public void modeRevision3_2_base() {
        ApduResponse apduResponse =
                new ApduResponse(ByteArrayUtil.fromHex(/* Challenge (8) */ "0011223344556677" +
                /* Current KVC (1) */ "55" +
                /* SV TNum (2) */ "A55A" +
                /* Previous SignatureLo (6) */ "665544332211" +
                /* SV Balance (3) */ "000000" +
                /* Load Date (2) */ "1234" +
                /* Load Free1 (1) */ "22" +
                /* Load KVC (1) */ "AA" +
                /* Load Free2 (1) */ "33" +
                /* Load Balance (3) */ "001121" +
                /* Load Amount (3) */ "000001" +
                /* Load Time (2) */ "5678" +
                /* Load SAM ID (4) */ "AABBCCDD" +
                /* Load SAM TNum (3) */ "D23456" +
                /* Load SV TNum (2) */ "E567" +
                /* Debit Amount (2) */ "0001" +
                /* Debit Date (2) */ "1235" +
                /* Debit Time (2) */ "6789" +
                /* Debit KVC (1) */ "BB" +
                /* Debit SAM ID (4) */ "BBCCDDEE" +
                /* Debit SAM TNum (3) */ "A34567" +
                /* Debit Balance (3) */ "001120" +
                /* Debit SV TNum (2) */ "F568" +
                /* Successful execution status word */ "9000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        /* check length */
        Assert.assertEquals(0x3D + 2, svGetRespPars.getApduResponse().getBytes().length);

        /* check common fields */
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("0011223344556677"),
                svGetRespPars.getChallengeOut());
        Assert.assertEquals(0, svGetRespPars.getBalance());
        Assert.assertEquals((byte) 0x55, svGetRespPars.getCurrentKVC());
        Assert.assertEquals(0xA55A, svGetRespPars.getTransactionNumber());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("665544332211"),
                svGetRespPars.getPreviousSignatureLo());
        // TODO Review this
        // /* check load log fields */
        // Assert.assertArrayEquals(ByteArrayUtil.fromHex("1234"),
        // svGetRespPars.getLoadLog().getDate());
        // Assert.assertArrayEquals(ByteArrayUtil.fromHex("5678"),
        // svGetRespPars.getLoadLog().getTime());
        // Assert.assertArrayEquals(ByteArrayUtil.fromHex("2233"),
        // svGetRespPars.getLoadLog().getFree());
        // Assert.assertEquals((byte) 0xAA, svGetRespPars.getLoadLog().getKVC());
        // Assert.assertEquals(0x1121, svGetRespPars.getLoadLog().getBalance());
        // Assert.assertEquals(0x01, svGetRespPars.getLoadLog().getAmount());
        // Assert.assertArrayEquals(ByteArrayUtil.fromHex("AABBCCDD"),
        // svGetRespPars.getLoadLog().getSamID());
        // Assert.assertEquals(0xD23456, svGetRespPars.getLoadLog().getSamTransactionNumber());
        // Assert.assertEquals(0xE567, svGetRespPars.getLoadLog().getSvTransactionNumber());
        //
        // /* check debit log fields */
        // Assert.assertArrayEquals(ByteArrayUtil.fromHex("1235"),
        // svGetRespPars.getDebitLog().getDate());
        // Assert.assertArrayEquals(ByteArrayUtil.fromHex("6789"),
        // svGetRespPars.getDebitLog().getTime());
        // Assert.assertEquals((byte) 0xBB, svGetRespPars.getDebitLog().getKVC());
        // Assert.assertEquals(0x1120, svGetRespPars.getDebitLog().getBalance());
        // Assert.assertEquals(0x01, svGetRespPars.getDebitLog().getAmount());
        // Assert.assertArrayEquals(ByteArrayUtil.fromHex("BBCCDDEE"),
        // svGetRespPars.getDebitLog().getSamID());
        // Assert.assertEquals(0xA34567, svGetRespPars.getDebitLog().getSamTransactionNumber());
        // Assert.assertEquals(0xF568, svGetRespPars.getDebitLog().getSvTransactionNumber());
    }

    @Test
    public void modeRevision3_2_B1_LB_1_LA_1_DB_1_DA_1() {
        /*
         * B1_LB1_LA1_DB1_DA1: balance 1, load balance 1, load amount 1, debit balance 1, debit
         * amount 1
         */
        ApduResponse apduResponse =
                new ApduResponse(ByteArrayUtil.fromHex("001122334455667755A55A665544332211" +
                /* B 1 */ "000001" + "123422AA33" +
                /* LB 1 LA 1 */ "000001000001" + "5678AABBCCDD1234564567" +
                /* DA 1 */ "0001" + "12356789BBBBCCDDEE234567" +
                /* DB 1 */ "000001" + "45689000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(1, svGetRespPars.getBalance());
        Assert.assertEquals(1, svGetRespPars.getLoadLog().getBalance());
        Assert.assertEquals(1, svGetRespPars.getLoadLog().getAmount());
        Assert.assertEquals(1, svGetRespPars.getDebitLog().getBalance());
        Assert.assertEquals(1, svGetRespPars.getDebitLog().getAmount());
    }

    @Test
    public void modeRevision3_2_B256_LB_257_LA_256_DB_256_DA_257() {
        ApduResponse apduResponse =
                new ApduResponse(ByteArrayUtil.fromHex("001122334455667755A55A665544332211" +
                /* B 256 */ "000100" + "123422AA33" +
                /* LB 257 LA 256 */ "000101000100" + "5678AABBCCDD1234564567" +
                /* DA 257 */ "0101" + "12356789BBBBCCDDEE234567" +
                /* DB 256 */ "000100" + "45689000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(256, svGetRespPars.getBalance());
        Assert.assertEquals(257, svGetRespPars.getLoadLog().getBalance());
        Assert.assertEquals(256, svGetRespPars.getLoadLog().getAmount());
        Assert.assertEquals(256, svGetRespPars.getDebitLog().getBalance());
        Assert.assertEquals(257, svGetRespPars.getDebitLog().getAmount());
    }

    @Test
    public void modeRevision3_2_B65536_LB_65537_LA_65536_DB_65536_DA_m32768() {
        ApduResponse apduResponse =
                new ApduResponse(ByteArrayUtil.fromHex("001122334455667755A55A665544332211" +
                /* B 65536 */ "010000" + "123422AA33" +
                /* LB 65537 LA 65536 */ "010001010000" + "5678AABBCCDD1234564567" +
                /* DA -32768 */ "8000" + "12356789BBBBCCDDEE234567" +
                /* DB 65536 */ "010000" + "45689000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(65536, svGetRespPars.getBalance());
        Assert.assertEquals(65537, svGetRespPars.getLoadLog().getBalance());
        Assert.assertEquals(65536, svGetRespPars.getLoadLog().getAmount());
        Assert.assertEquals(65536, svGetRespPars.getDebitLog().getBalance());
        Assert.assertEquals(-32768, svGetRespPars.getDebitLog().getAmount());
    }

    /* highest value */
    @Test
    public void modeRevision3_2_B8388607_LB8388607_LA_8388607_DA_32767_DB8388607() {
        ApduResponse apduResponse =
                new ApduResponse(ByteArrayUtil.fromHex("001122334455667755A55A665544332211" +
                /* 8388607 */ "7FFFFF" + "123422AA33" +
                /* LB 8388606 LA 8388607 */ "7FFFFF7FFFFF" + "5678AABBCCDD1234564567" +
                /* DA 32767 */ "7FFF" + "12356789BBBBCCDDEE234567" +
                /* DB 8388607 */ "7FFFFF" + "45689000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(8388607, svGetRespPars.getBalance());
        Assert.assertEquals(8388607, svGetRespPars.getLoadLog().getBalance());
        Assert.assertEquals(8388607, svGetRespPars.getLoadLog().getAmount());
        Assert.assertEquals(8388607, svGetRespPars.getDebitLog().getBalance());
        Assert.assertEquals(32767, svGetRespPars.getDebitLog().getAmount());
    }

    @Test
    public void modeRevision3_2_Bm1_LBm1_LAm1_DBm1_DAm1() {
        ApduResponse apduResponse =
                new ApduResponse(ByteArrayUtil.fromHex("001122334455667755A55A665544332211" +
                /* -1 */ "FFFFFF" + "123422AA33" +
                /* LB -1 LA -1 */ "FFFFFFFFFFFF" + "5678AABBCCDD1234564567" +
                /* DA -1 */ "FFFF" + "12356789BBBBCCDDEE234567" +
                /* DB -1 */ "FFFFFF" + "45689000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(-1, svGetRespPars.getBalance());
        Assert.assertEquals(-1, svGetRespPars.getLoadLog().getBalance());
        Assert.assertEquals(-1, svGetRespPars.getLoadLog().getAmount());
        Assert.assertEquals(-1, svGetRespPars.getDebitLog().getBalance());
        Assert.assertEquals(-1, svGetRespPars.getDebitLog().getAmount());
    }

    /* lowest values */
    @Test
    public void modeRevision3_2_Bm8388608() {
        ApduResponse apduResponse =
                new ApduResponse(ByteArrayUtil.fromHex("001122334455667755A55A665544332211" +
                /* -8388608 */ "800000" + "123422AA33" +
                /* LB -8388608 LA -8388608 */ "800000800000" + "5678AABBCCDD1234564567" +
                /* DA -32768 */ "8000" + "12356789BBBBCCDDEE234567" +
                /* DB -8388608 */ "800000" + "45689000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(-8388608, svGetRespPars.getBalance());
        Assert.assertEquals(-8388608, svGetRespPars.getLoadLog().getBalance());
        Assert.assertEquals(-8388608, svGetRespPars.getLoadLog().getAmount());
        Assert.assertEquals(-8388608, svGetRespPars.getDebitLog().getBalance());
        Assert.assertEquals(-32768, svGetRespPars.getDebitLog().getAmount());
    }

    @Test
    public void modeCompat_base_Reload() {
        ApduResponse apduResponse =
                new ApduResponse(ByteArrayUtil.fromHex(/* Current KVC (1) */ "55" +
                /* SV TNum (2) */ "A55A" +
                /* Previous SignatureLo (6) */ "665544" +
                /* Challenge out */ "1122" +
                /* SV Balance (3) */ "000000" +
                /* Load Date (2) */ "1234" +
                /* Load Free1 (1) */ "22" +
                /* Load KVC (1) */ "AA" +
                /* Load Free2 (1) */ "33" +
                /* Load Balance (3) */ "001121" +
                /* Load Amount (3) */ "000001" +
                /* Load Time (2) */ "5678" +
                /* Load SAM ID (4) */ "AABBCCDD" +
                /* Load SAM TNum (3) */ "D23456" +
                /* Load SV TNum (2) */ "E567" +
                /* Successful execution status word */ "9000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        /* check length */
        Assert.assertEquals(0x21 + 2, svGetRespPars.getApduResponse().getBytes().length);

        /* check common fields */
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("1122"), svGetRespPars.getChallengeOut());
        Assert.assertEquals(0, svGetRespPars.getBalance());
        Assert.assertEquals((byte) 0x55, svGetRespPars.getCurrentKVC());
        Assert.assertEquals(0xA55A, svGetRespPars.getTransactionNumber());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("665544"),
                svGetRespPars.getPreviousSignatureLo());
        // TODO Review this
        // /* check load log fields */
        // Assert.assertArrayEquals(ByteArrayUtil.fromHex("1234"),
        // svGetRespPars.getLoadLog().getDate());
        // Assert.assertArrayEquals(ByteArrayUtil.fromHex("5678"),
        // svGetRespPars.getLoadLog().getTime());
        // Assert.assertArrayEquals(ByteArrayUtil.fromHex("2233"),
        // svGetRespPars.getLoadLog().getFree());
        // Assert.assertEquals((byte) 0xAA, svGetRespPars.getLoadLog().getKVC());
        // Assert.assertEquals(0x1121, svGetRespPars.getLoadLog().getBalance());
        // Assert.assertEquals(0x01, svGetRespPars.getLoadLog().getAmount());
        // Assert.assertArrayEquals(ByteArrayUtil.fromHex("AABBCCDD"),
        // svGetRespPars.getLoadLog().getSamID());
        // Assert.assertEquals(0xD23456, svGetRespPars.getLoadLog().getSamTransactionNumber());
        // Assert.assertEquals(0xE567, svGetRespPars.getLoadLog().getSvTransactionNumber());
    }

    @Test
    public void modeCompat_Reload_B1_LB1_LA1() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("55A55A6655441122" +
        /* B1 */ "000001" + "123422AA33" +
        /* LB 1 */ "000001" +
        /* LA 1 */"000001" + "5678AABBCCDDD23456E5679000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(1, svGetRespPars.getBalance());
        Assert.assertEquals(1, svGetRespPars.getLoadLog().getBalance());
        Assert.assertEquals(1, svGetRespPars.getLoadLog().getAmount());
    }

    @Test
    public void modeCompat_Reload_B256_LB257_LA256() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("55A55A6655441122" +
        /* B1 */ "000100" + "123422AA33" +
        /* LB 1 */ "000101" +
        /* LA 1 */"000100" + "5678AABBCCDDD23456E5679000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(256, svGetRespPars.getBalance());
        Assert.assertEquals(257, svGetRespPars.getLoadLog().getBalance());
        Assert.assertEquals(256, svGetRespPars.getLoadLog().getAmount());
    }

    @Test
    public void modeCompat_Reload_B65536_LB65537_LA65536() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("55A55A6655441122" +
        /* B1 */ "010000" + "123422AA33" +
        /* LB 1 */ "010001" +
        /* LA 1 */"010000" + "5678AABBCCDDD23456E5679000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(65536, svGetRespPars.getBalance());
        Assert.assertEquals(65537, svGetRespPars.getLoadLog().getBalance());
        Assert.assertEquals(65536, svGetRespPars.getLoadLog().getAmount());
    }

    @Test
    public void modeCompat_Reload_B8388607_LB8388607_LA_8388607() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("55A55A6655441122" +
        /* B1 */ "7FFFFF" + "123422AA33" +
        /* LB 1 */ "7FFFFF" +
        /* LA 1 */"7FFFFF" + "5678AABBCCDDD23456E5679000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(8388607, svGetRespPars.getBalance());
        Assert.assertEquals(8388607, svGetRespPars.getLoadLog().getBalance());
        Assert.assertEquals(8388607, svGetRespPars.getLoadLog().getAmount());
    }

    @Test
    public void modeCompat_Reload_Bm8388608() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("55A55A6655441122" +
        /* B1 */ "800000" + "123422AA33" +
        /* LB 1 */ "800000" +
        /* LA 1 */"800000" + "5678AABBCCDDD23456E5679000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(-8388608, svGetRespPars.getBalance());
        Assert.assertEquals(-8388608, svGetRespPars.getLoadLog().getBalance());
        Assert.assertEquals(-8388608, svGetRespPars.getLoadLog().getAmount());
    }

    @Test
    public void modeCompat_Reload_Bm1() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("55A55A6655441122" +
        /* B1 */ "FFFFFF" + "123422AA33" +
        /* LB 1 */ "FFFFFF" +
        /* LA 1 */"FFFFFF" + "5678AABBCCDDD23456E5679000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(-1, svGetRespPars.getBalance());
        Assert.assertEquals(-1, svGetRespPars.getLoadLog().getBalance());
        Assert.assertEquals(-1, svGetRespPars.getLoadLog().getAmount());
    }

    @Test
    public void modeCompat_base_Debit() {
        ApduResponse apduResponse =
                new ApduResponse(ByteArrayUtil.fromHex(/* Current KVC (1) */ "55" +
                /* SV TNum (2) */ "A55A" +
                /* Previous SignatureLo (6) */ "665544" +
                /* Challenge out */ "1122" +
                /* SV Balance (3) */ "000000" +
                /* Debit Amount (2) */ "0001" +
                /* Debit Date (2) */ "1235" +
                /* Debit Time (2) */ "6789" +
                /* Debit KVC (1) */ "BB" +
                /* Debit SAM ID (4) */ "BBCCDDEE" +
                /* Debit SAM TNum (3) */ "A34567" +
                /* Debit Balance (3) */ "001120" +
                /* Debit SV TNum (2) */ "F568" +
                /* Successful execution status word */ "9000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        /* check length */
        Assert.assertEquals(0x1E + 2, svGetRespPars.getApduResponse().getBytes().length);

        /* check common fields */
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("1122"), svGetRespPars.getChallengeOut());
        Assert.assertEquals(0, svGetRespPars.getBalance());
        Assert.assertEquals((byte) 0x55, svGetRespPars.getCurrentKVC());
        Assert.assertEquals(0xA55A, svGetRespPars.getTransactionNumber());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("665544"),
                svGetRespPars.getPreviousSignatureLo());

        // TODO Review this
        // /* check debit log fields */
        // Assert.assertArrayEquals(ByteArrayUtil.fromHex("1235"),
        // svGetRespPars.getDebitLog().getDate());
        // Assert.assertArrayEquals(ByteArrayUtil.fromHex("6789"),
        // svGetRespPars.getDebitLog().getTime());
        // Assert.assertEquals((byte) 0xBB, svGetRespPars.getDebitLog().getKVC());
        // Assert.assertEquals(0x1120, svGetRespPars.getDebitLog().getBalance());
        // Assert.assertEquals(0x01, svGetRespPars.getDebitLog().getAmount());
        // Assert.assertArrayEquals(ByteArrayUtil.fromHex("BBCCDDEE"),
        // svGetRespPars.getDebitLog().getSamID());
        // Assert.assertEquals(0xA34567, svGetRespPars.getDebitLog().getSamTransactionNumber());
        // Assert.assertEquals(0xF568, svGetRespPars.getDebitLog().getSvTransactionNumber());
    }

    @Test
    public void modeCompat_Debit_B1_LB1_LA1() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("55A55A6655441122" +
        /* B1 */ "000001" +
        /* DA 1 */ "0001" + "12356789BBBBCCDDEEA34567" +
        /* DB 1 */ "000001" + "F5689000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(1, svGetRespPars.getBalance());
        Assert.assertEquals(1, svGetRespPars.getDebitLog().getBalance());
        Assert.assertEquals(1, svGetRespPars.getDebitLog().getAmount());
    }

    @Test
    public void modeCompat_Debit_B256_LB257_LA256() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("55A55A6655441122" +
        /* B1 */ "000100" +
        /* DA 1 */ "0101" + "12356789BBBBCCDDEEA34567" +
        /* DB 1 */ "000100" + "F5689000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(256, svGetRespPars.getBalance());
        Assert.assertEquals(256, svGetRespPars.getDebitLog().getBalance());
        Assert.assertEquals(257, svGetRespPars.getDebitLog().getAmount());
    }

    @Test
    public void modeCompat_Debit_B65536_LB8000_LA65536() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("55A55A6655441122" +
        /* B1 */ "010000" +
        /* DA 1 */ "8000" + "12356789BBBBCCDDEEA34567" +
        /* DB 1 */ "010000" + "F5689000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(65536, svGetRespPars.getBalance());
        Assert.assertEquals(65536, svGetRespPars.getDebitLog().getBalance());
        Assert.assertEquals(-32768, svGetRespPars.getDebitLog().getAmount());
    }

    @Test
    public void modeCompat_Debit_B8388607_LB32767_LA8388607() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("55A55A6655441122" +
        /* B1 */ "7FFFFF" +
        /* DA 1 */ "7FFF" + "12356789BBBBCCDDEEA34567" +
        /* DB 1 */ "7FFFFF" + "F5689000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(8388607, svGetRespPars.getBalance());
        Assert.assertEquals(8388607, svGetRespPars.getDebitLog().getBalance());
        Assert.assertEquals(32767, svGetRespPars.getDebitLog().getAmount());
    }

    @Test
    public void modeCompat_Debit_mB8388608() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("55A55A6655441122" +
        /* B1 */ "800000" +
        /* DA 1 */ "8000" + "12356789BBBBCCDDEEA34567" +
        /* DB 1 */ "800000" + "F5689000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(-8388608, svGetRespPars.getBalance());
        Assert.assertEquals(-8388608, svGetRespPars.getDebitLog().getBalance());
        Assert.assertEquals(-32768, svGetRespPars.getDebitLog().getAmount());
    }

    @Test
    public void modeCompat_Debit_m1() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("55A55A6655441122" +
        /* B1 */ "FFFFFF" +
        /* DA 1 */ "FFFF" + "12356789BBBBCCDDEEA34567" +
        /* DB 1 */ "FFFFFF" + "F5689000"), null);

        SvGetRespPars svGetRespPars = new SvGetRespPars(header, apduResponse, null);

        Assert.assertEquals(-1, svGetRespPars.getBalance());
        Assert.assertEquals(-1, svGetRespPars.getDebitLog().getBalance());
        Assert.assertEquals(-1, svGetRespPars.getDebitLog().getAmount());
    }
}
