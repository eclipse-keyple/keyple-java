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
package org.eclipse.keyple.core.command;

import static org.junit.Assert.*;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractIso7816CommandBuilderTest extends CoreBaseTest {

    private static final Logger logger =
            LoggerFactory.getLogger(AbstractIso7816CommandBuilderTest.class);

    @Before
    public void setUp() throws Exception {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }

    @Test
    public void testConstructor1() {
        ApduRequest apduRequest = new ApduRequest(ByteArrayUtil.fromHex("00112233445566"), false);
        Iso7816CommandBuilder iso7816CommandBuilder =
                new Iso7816CommandBuilder(CommandRef.COMMAND_1, apduRequest);

        Assert.assertEquals("COMMAND_1", iso7816CommandBuilder.getName());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("00112233445566"),
                iso7816CommandBuilder.getApduRequest().getBytes());
        Assert.assertFalse(iso7816CommandBuilder.getApduRequest().isCase4());
        Assert.assertNull(iso7816CommandBuilder.getApduRequest().getSuccessfulStatusCodes());
        Assert.assertEquals("COMMAND_1", iso7816CommandBuilder.getApduRequest().getName());

        apduRequest = new ApduRequest("APDU_2", ByteArrayUtil.fromHex("AABBCCDDEEFF00"), true);
        Assert.assertEquals("APDU_2", apduRequest.getName());
        iso7816CommandBuilder = new Iso7816CommandBuilder(CommandRef.COMMAND_2, apduRequest);

        Assert.assertEquals("COMMAND_2", iso7816CommandBuilder.getName());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("AABBCCDDEEFF00"),
                iso7816CommandBuilder.getApduRequest().getBytes());
        Assert.assertTrue(iso7816CommandBuilder.getApduRequest().isCase4());
        Assert.assertNull(iso7816CommandBuilder.getApduRequest().getSuccessfulStatusCodes());
        // TODO check if APDU_2 should be overwritten by COMMAND_2
        Assert.assertEquals("COMMAND_2", iso7816CommandBuilder.getApduRequest().getName());
    }

    @Test
    public void testConstructor2() {
        ApduRequest apduRequest = new ApduRequest(ByteArrayUtil.fromHex("00112233445566"), false);

        // TODO do we really need this second form of constructor?
        Iso7816CommandBuilder iso7816CommandBuilder =
                new Iso7816CommandBuilder("COMMAND_1", apduRequest);

        Assert.assertEquals("COMMAND_1", iso7816CommandBuilder.getName());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("00112233445566"),
                iso7816CommandBuilder.getApduRequest().getBytes());
        Assert.assertFalse(iso7816CommandBuilder.getApduRequest().isCase4());
        Assert.assertNull(iso7816CommandBuilder.getApduRequest().getSuccessfulStatusCodes());
        Assert.assertEquals("COMMAND_1", iso7816CommandBuilder.getApduRequest().getName());
    }

    @Test
    public void testAddSubName() {
        Iso7816CommandBuilder iso7816CommandBuilder =
                new Iso7816CommandBuilder(CommandRef.COMMAND_1, null);
        iso7816CommandBuilder.addSubName("TEST SUBNAME");
        Assert.assertEquals("COMMAND_1 - TEST SUBNAME", iso7816CommandBuilder.getName());
    }

    // TODO Rename or modify the setApduRequest method since it doesn't set the ApduRequest

    @Test(expected = IllegalArgumentException.class)
    public void testSetApduRequestDatainLeNon0() {
        Iso7816CommandBuilder iso7816CommandBuilder =
                new Iso7816CommandBuilder(CommandRef.COMMAND_1, null);
        ApduRequest apduRequest =
                iso7816CommandBuilder.setApduRequest((byte) 0xCC, CommandRef.COMMAND_1, (byte) 0xB1,
                        (byte) 0xB2, ByteArrayUtil.fromHex("11223344"), (byte) 1);
    }

    @Test
    public void testSetApduRequestCase1() {
        // case 1: dataIn = null, le = null
        Iso7816CommandBuilder iso7816CommandBuilder =
                new Iso7816CommandBuilder(CommandRef.COMMAND_1, null);
        ApduRequest apduRequest = iso7816CommandBuilder.setApduRequest((byte) 0xCC,
                CommandRef.COMMAND_1, (byte) 0xB1, (byte) 0xB2, null, null);
        logger.info("APDU case1: {}", ByteArrayUtil.toHex(apduRequest.getBytes()));
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("CC 11 B1 B2 00"),
                apduRequest.getBytes());
        Assert.assertFalse(apduRequest.isCase4());
    }

    @Test
    public void testSetApduRequestCase2() {
        // case 2: dataIn = null, le != null
        Iso7816CommandBuilder iso7816CommandBuilder =
                new Iso7816CommandBuilder(CommandRef.COMMAND_1, null);
        ApduRequest apduRequest = iso7816CommandBuilder.setApduRequest((byte) 0xCC,
                CommandRef.COMMAND_1, (byte) 0xB1, (byte) 0xB2, null, (byte) 6);
        logger.info("APDU case2: {}", ByteArrayUtil.toHex(apduRequest.getBytes()));
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("CC 11 B1 B2 06"), apduRequest.getBytes());
        Assert.assertFalse(apduRequest.isCase4());
    }

    @Test
    public void testSetApduRequestCase3() {
        // case 3: dataIn != null, le = null
        Iso7816CommandBuilder iso7816CommandBuilder =
                new Iso7816CommandBuilder(CommandRef.COMMAND_1, null);
        ApduRequest apduRequest =
                iso7816CommandBuilder.setApduRequest((byte) 0xCC, CommandRef.COMMAND_1, (byte) 0xB1,
                        (byte) 0xB2, ByteArrayUtil.fromHex("11223344"), null);
        logger.info("APDU case3: {}", ByteArrayUtil.toHex(apduRequest.getBytes()));
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("CC 11 B1 B2 04 11223344"),
                apduRequest.getBytes());
        Assert.assertFalse(apduRequest.isCase4());
    }

    @Test
    public void testSetApduRequestCase4() {
        // case 4: dataIn = null, le = 0
        Iso7816CommandBuilder iso7816CommandBuilder =
                new Iso7816CommandBuilder(CommandRef.COMMAND_1, null);
        ApduRequest apduRequest =
                iso7816CommandBuilder.setApduRequest((byte) 0xCC, CommandRef.COMMAND_1, (byte) 0xB1,
                        (byte) 0xB2, ByteArrayUtil.fromHex("11223344"), (byte) 0);
        logger.info("APDU case4: {}", ByteArrayUtil.toHex(apduRequest.getBytes()));
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("CC 11 B1 B2 04 11223344 00"),
                apduRequest.getBytes());
        Assert.assertTrue(apduRequest.isCase4());
    }

    @Test
    public void testSetApduRequest() {
        Iso7816CommandBuilder iso7816CommandBuilder =
                new Iso7816CommandBuilder(CommandRef.COMMAND_1, null);
        ApduRequest apduRequest =
                iso7816CommandBuilder.setApduRequest((byte) 0xCC, CommandRef.COMMAND_1, (byte) 0xB1,
                        (byte) 0xB2, ByteArrayUtil.fromHex("11223344"), (byte) 0);
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("CC11B1B2041122334400"),
                apduRequest.getBytes());
    }

    private enum CommandRef implements SeCommand {
        COMMAND_1("COMMAND_1", (byte) 0x11), COMMAND_2("COMMAND_2", (byte) 0x22);

        /** The name. */
        private final String name;

        /** The instruction byte. */
        private final byte instructionbyte;

        CommandRef(String name, byte instructionByte) {
            this.name = name;
            this.instructionbyte = instructionByte;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public byte getInstructionByte() {
            return instructionbyte;
        }
    }

    private class Iso7816CommandBuilder extends AbstractIso7816CommandBuilder {
        public Iso7816CommandBuilder(SeCommand commandReference, ApduRequest request) {
            super(commandReference, request);
        }

        public Iso7816CommandBuilder(String name, ApduRequest request) {
            super(name, request);
        }
    }
}
