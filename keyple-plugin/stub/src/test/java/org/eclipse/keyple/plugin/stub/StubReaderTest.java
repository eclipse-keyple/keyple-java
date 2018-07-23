/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.stub;



import java.io.IOException;
import java.util.*;
import org.eclipse.keyple.calypso.commands.po.PoRevision;
import org.eclipse.keyple.calypso.commands.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StubReaderTest {

    StubReader reader;
    StubSecureElement se;

    @Before
    public void SetUp() throws IOReaderException {
        // works if stubreader could be instanciated just once
        reader = new StubReader();
        se = new HoplinkStubSE();
    }


    /*
     * TRANSMIT
     */


    @Test
    public void testInsert() throws NoStackTraceThrowable {
        insertSe();
        Assert.assertTrue(reader.isSePresent());
    }

    @Test(expected = IOReaderException.class)
    public void testTransmitNull() throws Exception {
        insertSe();
        reader.transmit((SeRequestSet) null).getSingleResponse().getApduResponses().size();
    }

    @Test
    public void transmitSuccessfull() throws IOException {
        // init
        SeRequestSet requests = getRequestIsoDepSetSample();

        // test
        insertSe();
        SeResponseSet seResponse = reader.transmit(requests);

        // assert
        Assert.assertTrue(seResponse.getSingleResponse().getFci().isSuccessful());
    }


    @Test(expected = IOReaderException.class)
    public void testTransmitSEnotPressent() throws IOReaderException {
        SeRequestSet seRequest = getRequestIsoDepSetSample();
        Assert.assertTrue(
                reader.transmit(seRequest).getSingleResponse().getApduResponses().size() == 0);
    }


    /*
     * NAME and PARAMETERS
     */

    @Test
    public void testGetName() {
        Assert.assertNotNull(reader.getName());
    }

    // Set wrong parameter
    @Test(expected = IOReaderException.class)
    public void testSetWrongParameter() throws Exception {
        reader.setParameter("WRONG_PARAMETER", "a");
    }

    // Set wrong parameters
    @Test(expected = IOReaderException.class)
    public void testSetWrongParameters() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("WRONG_PARAMETER", "d");
        parameters.put(StubReader.ALLOWED_PARAMETER_1, "a");
        reader.setParameters(parameters);
    }

    // Set correct paramaters
    public void testSetParameters() throws Exception {
        Map<String, String> p1 = new HashMap<String, String>();
        p1.put(StubReader.ALLOWED_PARAMETER_1, "a");
        p1.put(StubReader.ALLOWED_PARAMETER_2, "a");

        reader.setParameters(p1);
        Map<String, String> p2 = reader.getParameters();
        assert (p1.equals(p2));


    }


    /*
     * HELPERS
     */


    private SeRequestSet getRequestIsoDepSetSample() {
        String poAid = "A000000291A000000191";

        ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(PoRevision.REV3_1,
                (byte) 0x14, (byte) 0x01, true, (byte) 0x20);

        List<ApduRequest> poApduRequestList;

        poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());

        SeRequest.Selector selector = new SeRequest.AidSelector(ByteBufferUtils.fromHex(poAid));

        SeRequest seRequest = new SeRequest(selector, poApduRequestList, false,
                ContactlessProtocols.PROTOCOL_ISO14443_4);

        return new SeRequestSet(seRequest);

    }

    private void insertSe() {
        reader.insertSe(se);
    }
}
