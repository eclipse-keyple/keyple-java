/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.stub;



import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import org.eclipse.keyple.calypso.commands.po.PoRevision;
import org.eclipse.keyple.calypso.commands.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.eclipse.keyple.util.Observable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
@RunWith(MockitoJUnitRunner.class)
public class StubReaderTest {

    StubReader reader;


    // init before each test
    @Before
    public void SetUp() throws IOReaderException {
        // works if stubreader could be instanciated just once
        reader = new StubReader();

    }


    /*
     * TRANSMIT
     */


    @Test
    public void testInsert() throws NoStackTraceThrowable {

        // add observer
        reader.addObserver(new Observable.Observer<ReaderEvent>() {
            @Override
            public void update(ReaderEvent event) {
                Assert.assertEquals(event.getReaderName(), reader.getName());
                Assert.assertEquals(event.getPluginName(), StubPlugin.getInstance().getName());
                Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());

            }
        });

        // test
        reader.insertSe(hoplinkSE());

        // assert
        Assert.assertTrue(reader.isSePresent());


    }


    @Test(expected = IOReaderException.class)
    public void transmit_Hoplink_null() throws Exception {
        reader.insertSe(hoplinkSE());
        reader.transmit((SeRequestSet) null).getSingleResponse().getApduResponses().size();

        // throws exception
    }

    @Test
    public void transmit_Hoplink_Sucessfull() throws IOException {
        // init Request
        SeRequestSet requests = getRequestIsoDepSetSample();

        // init SE
        reader.insertSe(hoplinkSE());

        // test
        SeResponseSet seResponse = reader.transmit(requests);

        // assert
        Assert.assertTrue(seResponse.getSingleResponse().getApduResponses().get(0).isSuccessful());
    }


    @Test
    public void transmit_null_Selection() throws IOReaderException {
        // init SE
        // no SE

        // init request
        SeRequestSet seRequest = getRequestIsoDepSetSample();

        // test
        SeResponseSet resp = reader.transmit(seRequest);

        Assert.assertNotNull(resp);

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
    @Test
    public void testSetParameters() throws Exception {
        Map<String, String> p1 = new HashMap<String, String>();
        p1.put(StubReader.ALLOWED_PARAMETER_1, "a");
        p1.put(StubReader.ALLOWED_PARAMETER_2, "a");

        reader.setParameters(p1);
        Map<String, String> p2 = reader.getParameters();
        assert (p1.equals(p2));


    }

    /*
     * INTERNAL METHODS
     */

    @Test(expected = ChannelStateReaderException.class)
    public void processApduRequestTest() throws Exception {
        // init request
        ApduRequest apdu = getApduSample();

        // init SE
        reader.insertSe(getSENoconnection());

        // test
        ApduResponse response = reader.processApduRequestTestProxy(apdu);

        // assert
        Assert.assertNull(response);

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

    private StubSecureElement hoplinkSE() {
        return new HoplinkStubSE();
    }

    private StubSecureElement getSENoconnection() {
        return new StubSecureElement() {
            @Override
            public ByteBuffer getATR() {
                return null;
            }

            @Override
            public boolean isPhysicalChannelOpen() {
                return false;
            }

            @Override
            public void openPhysicalChannel()
                    throws IOReaderException, ChannelStateReaderException {
                throw new IOReaderException("Impossible to estasblish connection");
            }

            @Override
            public void closePhysicalChannel() throws IOReaderException {
                throw new IOReaderException("Channel is not open");
            }

            @Override
            public ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException {
                throw new ChannelStateReaderException("Error while transmitting apdu");
            }

            @Override
            public SeProtocol getSeProcotol() {
                return null;
            }
        };

    }

    static ApduRequest getApduSample() {
        return new ApduRequest(ByteBufferUtils.fromHex("FEDCBA98 9005h"), false);
    }



}
