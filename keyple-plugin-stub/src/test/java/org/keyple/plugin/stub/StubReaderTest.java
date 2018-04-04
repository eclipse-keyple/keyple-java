package org.keyple.plugin.stub;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ObservableReader;
import org.keyple.seproxy.SeRequest;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class StubReaderTest {

    StubReader stubReader;

    @Before
    public void SetUp() throws IOReaderException {
        stubReader = (StubReader) StubPlugin.getInstance().getReaders().get(0);
    }


    @Test
    public void testGetName(){
        assert(stubReader.getName() != null);
    }

    @Test
    public void testIsPresent() throws IOReaderException {
        assert(!stubReader.isSEPresent());
    }

    @Test
    public void testTransmitNull() throws IOReaderException {
        assert (stubReader.transmit(null) == null);
    }

    @Test
    //if APDURequest is empty, APDU response is empty
    public void testTransmitEmptySERequest() throws IOReaderException {
        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        SeRequest seRequest = new SeRequest(apduRequests);
        assert (stubReader.transmit(seRequest).getApduResponses().size() == 0);
    }


}
