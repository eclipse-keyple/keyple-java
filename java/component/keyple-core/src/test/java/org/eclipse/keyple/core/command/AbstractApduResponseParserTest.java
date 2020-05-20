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

import java.util.Map;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.command.exception.KeypleSeCommandException;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractApduResponseParserTest extends CoreBaseTest {

    private static final Logger logger =
            LoggerFactory.getLogger(AbstractApduResponseParserTest.class);

    @Before
    public void setUp() throws Exception {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }

    @Test
    public void testApduSuccessful1() {
        // standard successful status word (9000)
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("9000"), null);
        ApduResponseParser apduResponseParser = getApduResponseParser(apduResponse);

        Assert.assertArrayEquals(ByteArrayUtil.fromHex("9000"),
                apduResponseParser.getApduResponse().getBytes());
        Assert.assertTrue(apduResponseParser.isSuccessful());
        Assert.assertEquals("Success", apduResponseParser.getStatusInformation());
    }

    @Test
    public void testApduSuccessful2() {

        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("9999"), null);
        ApduResponseParser apduResponseParser = getApduResponseParser(apduResponse);

        Assert.assertArrayEquals(ByteArrayUtil.fromHex("9999"),
                apduResponseParser.getApduResponse().getBytes());
        Assert.assertTrue(apduResponseParser.isSuccessful());
        Assert.assertEquals("sw 9999", apduResponseParser.getStatusInformation());
    }

    @Test
    public void testApduUnsuccessful() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("6500"), null);
        ApduResponseParser apduResponseParser = getApduResponseParser(apduResponse);

        Assert.assertArrayEquals(ByteArrayUtil.fromHex("6500"),
                apduResponseParser.getApduResponse().getBytes());
        Assert.assertFalse(apduResponseParser.isSuccessful());
        Assert.assertEquals("sw 6500", apduResponseParser.getStatusInformation());
    }

    @Test
    public void testGetStatusTable() {
        ApduResponse apduResponse = Mockito.mock(ApduResponse.class);

        ApduResponseParser apduResponseParser = getApduResponseParser(apduResponse);

        Map<Integer, AbstractApduResponseParser.StatusProperties> statusTable =
                apduResponseParser.getStatusTable();

        Assert.assertEquals(4, statusTable.size());
        Assert.assertTrue(statusTable.containsKey(0x9000));
        Assert.assertTrue(statusTable.containsKey(0x9999));
        Assert.assertTrue(statusTable.containsKey(0x6500));
        Assert.assertTrue(statusTable.containsKey(0x6400));
        Assert.assertTrue(statusTable.get(0x9000).isSuccessful());
        Assert.assertTrue(statusTable.get(0x9999).isSuccessful());
        Assert.assertFalse(statusTable.get(0x6500).isSuccessful());
        Assert.assertFalse(statusTable.get(0x6400).isSuccessful());
    }

    /**
     * Build a custom and simple AbstractApduResponseParser
     * 
     * @param response ApduResponse to build ApduResponseParser
     * @return ApduResponseParser
     */
    static public ApduResponseParser getApduResponseParser(ApduResponse response) {
        return new ApduResponseParser(response);
    }

    static public final class ApduResponseParser extends AbstractApduResponseParser {
        public ApduResponseParser(ApduResponse response) {
            super(response, null);
            // additional status words
            STATUS_TABLE.put(0x9999, new StatusProperties("sw 9999"));
            STATUS_TABLE.put(0x6500,
                    new StatusProperties("sw 6500", KeypleSeCommandException.class));
            STATUS_TABLE.put(0x6400,
                    new StatusProperties("sw 6400", KeypleSeCommandException.class));
        }
    }
}
