/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@RunWith(MockitoJUnitRunner.class)
public class ApduResponseTest {



    @Before
    public void setUp() {}



    @Test
    public void constructorSuccessFullResponse() {
        ApduResponse response = new ApduResponse(ByteArrayUtils.fromHex("FEDCBA98 9000h"), null);
        assertNotNull(response);
        assertEquals(0x9000, response.getStatusCode());
        assertEquals("FEDCBA989000", ByteArrayUtils.toHex(response.getBytes()));
        assertArrayEquals(ByteArrayUtils.fromHex("FEDCBA98"), response.getDataOut());
        assertTrue(response.isSuccessful());
    }

    @Test
    public void constructorSuccessFullResponseWithCustomCode() {
        ApduResponse response =
                new ApduResponse(ByteArrayUtils.fromHex("FEDCBA98 9005h"), getA9005CustomCode());
        assertNotNull(response);
        assertEquals(0x9005, response.getStatusCode());
        assertEquals("FEDCBA989005", ByteArrayUtils.toHex(response.getBytes()));
        assertArrayEquals(ByteArrayUtils.fromHex("FEDCBA98"), response.getDataOut());
        assertTrue(response.isSuccessful());
    }

    @Test
    public void constructorFailResponse() {
        ApduResponse response = new ApduResponse(ByteArrayUtils.fromHex("FEDCBA98 9004h"), null);
        assertNotNull(response);
        assertEquals("FEDCBA989004", ByteArrayUtils.toHex(response.getBytes()));
        assertArrayEquals(ByteArrayUtils.fromHex("FEDCBA98"), response.getDataOut());
        assertEquals(0x9004, response.getStatusCode());
        assertFalse(response.isSuccessful());
    }

    @Test
    public void constructorFailResponseWithCustomCode() {
        ApduResponse response =
                new ApduResponse(ByteArrayUtils.fromHex("FEDCBA98 9004h"), getA9005CustomCode());
        assertNotNull(response);
        assertEquals("FEDCBA989004", ByteArrayUtils.toHex(response.getBytes()));
        assertArrayEquals(ByteArrayUtils.fromHex("FEDCBA98"), response.getDataOut());
        assertEquals(0x9004, response.getStatusCode());
        assertFalse(response.isSuccessful());
    }

    @Test
    public void isEqualsTest() {
        assertTrue(getAFCI().equals(getAFCI()));
    }

    @Test
    public void isThisEquals() {
        ApduResponse resp = getAFCI();
        assertTrue(resp.equals(resp));
    }

    @Test
    public void isNotEquals() {
        ApduResponse resp = getAFCI();
        Object obj = new Object();
        assertFalse(resp.equals(obj));
    }

    @Test
    public void isNotEqualsNull() {
        ApduResponse resp = getAFCI();
        ApduResponse respNull = new ApduResponse(null, null);
        assertFalse(resp.equals(respNull));
    }

    @Test
    public void hashcodeTest() {
        ApduResponse resp = getAFCI();
        ApduResponse resp2 = getAFCI();
        assertTrue(resp.hashCode() == resp2.hashCode());
    }

    @Test
    public void hashcodeNull() {
        ApduResponse resp = new ApduResponse(null, null);
        assertNotNull(resp.hashCode());
    }

    @Test
    public void testToStringNull() {
        ApduResponse resp = new ApduResponse(null, null);
        assertNotNull(resp.toString());
    }

    /*
     * HELPERS
     */


    public static Set<Short> getA9005CustomCode() {
        Set<Short> successfulStatusCodes = new HashSet<Short>();
        successfulStatusCodes.add((short) 0x9005);
        return successfulStatusCodes;
    }

    static ApduResponse getAAtr() {
        return new ApduResponse(ByteArrayUtils.fromHex("3B8F8001804F0CA000000306030001000000006A"),
                null);
    }

    static ApduResponse getAFCI() {
        return new ApduResponse(ByteArrayUtils.fromHex("9000"), null);
    }

    static ApduResponse getSuccessfullResponse() {
        return new ApduResponse(ByteArrayUtils.fromHex("FEDCBA98 9000h"), null);
    }

    public static List<ApduResponse> getAListOfAPDUs() {
        List<ApduResponse> apdus = new ArrayList<ApduResponse>();
        apdus.add(getSuccessfullResponse());
        return apdus;
    }

}
