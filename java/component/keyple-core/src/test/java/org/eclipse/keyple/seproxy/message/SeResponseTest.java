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
package org.eclipse.keyple.seproxy.message;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.SignatureDeclareThrowsException"})
@RunWith(MockitoJUnitRunner.class)
public class SeResponseTest {


    @Test
    public void constructorSuccessfullResponseMatch() throws IllegalArgumentException {

        SeResponse response = new SeResponse(true, true,
                new SelectionStatus(ApduResponseTest.getAAtr(), ApduResponseTest.getAFCI(), true),
                ApduResponseTest.getAListOfAPDUs());
        Assert.assertNotNull(response);
        Assert.assertArrayEquals(ApduResponseTest.getAListOfAPDUs().toArray(),
                response.getApduResponses().toArray());
        Assert.assertEquals(true, response.wasChannelPreviouslyOpen());
        Assert.assertEquals(ApduResponseTest.getAAtr(), response.getSelectionStatus().getAtr());
        Assert.assertEquals(ApduResponseTest.getAFCI(), response.getSelectionStatus().getFci());
        Assert.assertEquals(response.getSelectionStatus().hasMatched(), true);
    }

    @Test
    public void constructorSuccessfullResponseNoMatch() throws IllegalArgumentException {

        SeResponse response = new SeResponse(true, true,
                new SelectionStatus(ApduResponseTest.getAAtr(), ApduResponseTest.getAFCI(), false),
                ApduResponseTest.getAListOfAPDUs());
        Assert.assertNotNull(response);
        Assert.assertArrayEquals(ApduResponseTest.getAListOfAPDUs().toArray(),
                response.getApduResponses().toArray());
        Assert.assertEquals(true, response.wasChannelPreviouslyOpen());
        Assert.assertEquals(ApduResponseTest.getAAtr(), response.getSelectionStatus().getAtr());
        Assert.assertEquals(ApduResponseTest.getAFCI(), response.getSelectionStatus().getFci());
        Assert.assertEquals(response.getSelectionStatus().hasMatched(), false);
    }

    @Test
    public void constructorATRNull() throws IllegalArgumentException {
        SeResponse response = new SeResponse(true, true,
                new SelectionStatus(null, ApduResponseTest.getAFCI(), true),
                ApduResponseTest.getAListOfAPDUs());
        Assert.assertNotNull(response);
    }

    @Test
    public void constructorFCINull() throws IllegalArgumentException {
        SeResponse response = new SeResponse(true, true,
                new SelectionStatus(ApduResponseTest.getAAtr(), null, true),
                ApduResponseTest.getAListOfAPDUs());
        Assert.assertNotNull(response);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFCIAndATRNull() throws IllegalArgumentException {
        SeResponse response = new SeResponse(true, true, new SelectionStatus(null, null, true),
                ApduResponseTest.getAListOfAPDUs());
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
        SeResponse respNull = new SeResponse(true, true,
                new SelectionStatus(null, ApduResponseTest.getAFCI(), true), null);
        SeResponse respNull2 = new SeResponse(true, true,
                new SelectionStatus(ApduResponseTest.getAAtr(), null, true), null);
        SeResponse respNull3 = new SeResponse(true, true,
                new SelectionStatus(ApduResponseTest.getAAtr(), ApduResponseTest.getAFCI(), true),
                null);
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
        SeResponse resp = new SeResponse(true, true,
                new SelectionStatus(null, ApduResponseTest.getAFCI(), true), null);
        Assert.assertNotNull(resp.hashCode());
    }


    /*
     * HELPERS
     */

    public static SeResponse getASeResponse() throws IllegalArgumentException {
        return new SeResponse(true, true,
                new SelectionStatus(ApduResponseTest.getAAtr(), ApduResponseTest.getAFCI(), true),
                ApduResponseTest.getAListOfAPDUs());
    }
}
