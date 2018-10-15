/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.seproxy;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
@RunWith(MockitoJUnitRunner.class)
public class ApduRequestTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testSimpleAPDURequest() {
        ApduRequest request = new ApduRequest(getACommand(), true);
        assertNotNull(request);
        assertEquals(null, request.getName());
        assertTrue(request.isCase4());
        assertArrayEquals(getACommand(), request.getBytes());
        assertEquals(null, request.getSuccessfulStatusCodes());
        assertEquals("ApduRequest: NAME = \"null\", RAWDATA = FEDCBA989005, case4",
                request.toString());
    }

    @Test
    public void testAPDURequest() {
        ApduRequest request = getApduSample();
        assertNotNull(request);
        assertTrue(request.isCase4());
        assertArrayEquals(getACommand(), request.getBytes());
        assertEquals(getAName(), request.getName());
        assertEquals(getASuccessFulStatusCode(), request.getSuccessfulStatusCodes());
        assertEquals("ApduRequest: NAME = \"" + getAName()
                + "\", RAWDATA = FEDCBA989005, case4, additional successful status codes = 2328",
                request.toString());
    }



    /*
     * HELPERS
     */

    public static ApduRequest getApduSample() {
        Set<Short> successfulStatusCodes = getASuccessFulStatusCode();
        Boolean case4 = true;
        byte[] command = getACommand();
        ApduRequest request = new ApduRequest(command, case4, successfulStatusCodes);
        request.setName(getAName());
        return request;
    }

    static byte[] getACommand() {
        return ByteArrayUtils.fromHex("FEDCBA98 9005h");
    }

    static Set<Short> getASuccessFulStatusCode() {
        Set<Short> successfulStatusCodes = new HashSet<Short>();
        successfulStatusCodes.add(Short.valueOf("9000"));
        return successfulStatusCodes;
    }

    static String getAName() {
        return "TEST";
    }

}
