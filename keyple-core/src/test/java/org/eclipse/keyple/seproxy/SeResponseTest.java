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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
@RunWith(MockitoJUnitRunner.class)
public class SeResponseTest {


    @Test
    public void constructorSuccessfullResponse() throws IllegalArgumentException {

        SeResponse response = new SeResponse(true, ApduResponseTest.getAAtr(),
                ApduResponseTest.getAFCI(), ApduResponseTest.getAListOfAPDUs());
        Assert.assertNotNull(response);
        Assert.assertArrayEquals(ApduResponseTest.getAListOfAPDUs().toArray(),
                response.getApduResponses().toArray());
        Assert.assertEquals(true, response.wasChannelPreviouslyOpen());
        Assert.assertEquals(ApduResponseTest.getAAtr(), response.getAtr());
        Assert.assertEquals(ApduResponseTest.getAFCI(), response.getFci());
    }

    @Test
    public void constructorATRNull() throws IllegalArgumentException {
        SeResponse response = new SeResponse(true, null, ApduResponseTest.getAFCI(),
                ApduResponseTest.getAListOfAPDUs());
        Assert.assertNotNull(response);
    }

    @Test
    public void constructorFCINull() throws IllegalArgumentException {
        SeResponse response = new SeResponse(true, ApduResponseTest.getAAtr(), null,
                ApduResponseTest.getAListOfAPDUs());
        Assert.assertNotNull(response);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFCIAndATRNull() throws IllegalArgumentException {
        SeResponse response = new SeResponse(true, null, null, ApduResponseTest.getAListOfAPDUs());
        Assert.assertNull(response);
    }

    @Test()
    public void testEquals() throws Exception {
        Assert.assertTrue(getASeResponse().equals(getASeResponse()));
    }

    @Test()
    public void testThisEquals() throws Exception {
        SeResponse resp = getASeResponse();
        Assert.assertTrue(resp.equals(resp));
    }

    @Test()
    public void testNotEquals() throws Exception {
        SeResponse resp = getASeResponse();
        Object any = new Object();
        Assert.assertFalse(resp.equals(any));
    }

    @Test()
    public void testNotEqualsNull() throws Exception {
        SeResponse resp = getASeResponse();
        SeResponse respNull = new SeResponse(true, null, ApduResponseTest.getAFCI(), null);
        SeResponse respNull2 = new SeResponse(true, ApduResponseTest.getAAtr(), null, null);
        SeResponse respNull3 =
                new SeResponse(true, ApduResponseTest.getAAtr(), ApduResponseTest.getAFCI(), null);
        Assert.assertFalse(resp.equals(respNull));
        Assert.assertFalse(resp.equals(respNull2));
        Assert.assertFalse(resp.equals(respNull3));
    }

    @Test()
    public void hashcode() throws Exception {
        SeResponse resp = getASeResponse();
        SeResponse resp2 = getASeResponse();
        Assert.assertTrue(resp.hashCode() == resp2.hashCode());
    }

    @Test()
    public void hashcodeNull() throws Exception {
        SeResponse resp = new SeResponse(true, null, ApduResponseTest.getAFCI(), null);
        Assert.assertNotNull(resp.hashCode());
    }


    /*
     * HELPERS
     */

    public static SeResponse getASeResponse() throws IllegalArgumentException {
        return new SeResponse(true, ApduResponseTest.getAAtr(), ApduResponseTest.getAFCI(),
                ApduResponseTest.getAListOfAPDUs());
    }
}
