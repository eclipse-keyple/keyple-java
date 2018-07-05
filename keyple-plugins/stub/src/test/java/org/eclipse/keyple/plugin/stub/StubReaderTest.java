/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.stub;


import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.*;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StubReaderTest {

    StubReader stubReader;

    @Before
    public void SetUp() throws IOReaderException {
        // works if stubreader could be instanciated just once
        stubReader = (StubReader) StubPlugin.getInstance().getReaders().first();
    }


    @Test
    public void testGetName() {
        assert (stubReader.getName() != null);
    }

    @Test
    public void testIsPresent() throws NoStackTraceThrowable {
        assert (!stubReader.isSePresent());
    }

    // TODO redesign @Test
    public void testTransmitNull() throws IOReaderException {
        try {
            stubReader.transmit((SeRequestSet) null).getSingleResponse().getApduResponses().size();
            fail("Should raise exception");
        } catch (IOReaderException e) {
            e.printStackTrace();
            assert (e.getMessage().contains("null"));
        }
    }



    // TODO redesign @Test(expected = IOReaderException.class)
    // if SE is not present, transmit fails
    public void testTransmitSEnotPressent() throws IOReaderException {
        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        SeRequestSet seRequest = new SeRequestSet(new SeRequest(null, apduRequests, false));
        assert (stubReader.transmit(seRequest).getSingleResponse().getApduResponses().size() == 0);

    }

    // Timeout
    // TODO redesign @Test
    public void testTimeout() {
        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        SeRequestSet seRequest = new SeRequestSet(new SeRequest(null, apduRequests, false));
        stubReader.test_SetWillTimeout(true);

        try {
            stubReader.transmit(seRequest);
            fail("Should raise exception");
        } catch (IOReaderException e) {
            assert (e != null);
        }

    }

    // SE is not present
    // TODO redesign @Test
    public void testTransmitWithoutSE() {
        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        SeRequestSet seRequest = new SeRequestSet(new SeRequest(null, apduRequests, false));
        stubReader.test_RemoveSE();

        try {
            stubReader.transmit(seRequest);
            fail("Should raise exception");
        } catch (IOReaderException e) {
            assert (e != null);
        }
    }

    // Set wrong parameter
    @Test
    public void testSetWrongParamater() {
        try {
            stubReader.setParameter("WRONG_PARAMETER", "a");
            fail("Should raise exception");
        } catch (IOReaderException e) {
            assert (e != null);
        }
    }

    // Set A wrong parameter
    @Test
    public void testSetWrongParamaters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("WRONG_PARAMETER", "d");
        parameters.put(StubReader.ALLOWED_PARAMETER_1, "a");
        try {
            stubReader.setParameters(parameters);
            fail("Should raise exception");
        } catch (IOException e) {
            assert (e != null);
        }
    }

    // Set Paramater
    public void testSetParameters() {
        Map<String, String> p1 = new HashMap<String, String>();
        p1.put(StubReader.ALLOWED_PARAMETER_1, "a");
        p1.put(StubReader.ALLOWED_PARAMETER_2, "a");
        try {
            stubReader.setParameters(p1);
            Map<String, String> p2 = stubReader.getParameters();
            assert (p1.equals(p2));

        } catch (IOException e) {
            fail("should not raise exception");
        }
    }
}
