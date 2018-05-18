/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.csm.builder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.csm.builder.SelectDiversifierCmdBuild;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.GetDataFciCmdBuild;
import org.keyple.calypso.commands.po.parser.GetDataFciRespPars;
import org.keyple.commands.AbstractApduCommandBuilder;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.SeRequestSet;
import org.keyple.seproxy.SeResponseSet;
import org.keyple.seproxy.exceptions.*;
import org.mockito.Mockito;

public class SelectDiversiferCmdBuildTest {

    private ByteBuffer dataIn;

    private ProxyReader fakeSpecificReader;

    private List<ApduRequest> ApduRequests = new ArrayList<ApduRequest>();

    private SeRequestSet seRequest;

    private List<ApduResponse> apduResponses;

    private SeResponseSet seResponseExpected;

    private byte[] returnOK = {(byte) 0x90, 0x00};

    private ApduResponse responseExpected = new ApduResponse(null, true, returnOK);

    private AbstractApduCommandBuilder apduCommandBuilder =
            new GetDataFciCmdBuild(PoRevision.REV3_1);

    private List<ApduResponse> list = new ArrayList<ApduResponse>();

    private List<ApduRequest> ApduRequests2 = new ArrayList<ApduRequest>();

    @Test
    public void selectDiviersifier() throws IOReaderException, UnexpectedReaderException,
            ChannelStateReaderException, InvalidApduReaderException, ReaderTimeoutException,
            InconsistentCommandException, InconsistentParameterValueException {

        ApduRequest apdu = apduCommandBuilder.getApduRequest();
        ApduRequests.add(apdu);

        seRequest = new SeRequestSet(null, ApduRequests, true);
        list.add(
                new ApduResponse(
                        new byte[] {0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E,
                                0x49, 0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13,
                                (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A,
                                (byte) 0xB9, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01},
                        true, new byte[] {(byte) 0x90, 0x00}));
        list.add(
                new ApduResponse(
                        new byte[] {0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E,
                                0x49, 0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13,
                                (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A,
                                (byte) 0xB9, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01},
                        true, new byte[] {(byte) 0x80, 0x00}));

        SeResponseSet seResponse = new SeResponseSet(true, null, list);

        SeResponseSet responseFci = Mockito.mock(SeResponseSet.class);
        fakeSpecificReader = Mockito.mock(ProxyReader.class);

        Mockito.when(responseFci.getApduResponses()).thenReturn(list);
        Mockito.when(fakeSpecificReader.transmit(seRequest)).thenReturn(seResponse);

        GetDataFciRespPars.FCI fci =
                GetDataFciRespPars.toFCI(responseFci.getApduResponses().get(0).getBuffer());
        dataIn = fci.getApplicationSN();

        AbstractApduCommandBuilder apduCommandBuilder2 =
                new SelectDiversifierCmdBuild(null, dataIn);
        ApduRequest ApduRequest = apduCommandBuilder2.getApduRequest();

        ApduRequests2.add(ApduRequest);

        apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseExpected);

        seResponseExpected = new SeResponseSet(true, responseExpected, apduResponses);
        SeRequestSet seRequest2 = new SeRequestSet(null, ApduRequests2, true);

        Mockito.when(fakeSpecificReader.transmit(seRequest2)).thenReturn(seResponse);
        SeResponseSet seResponse1 = fakeSpecificReader.transmit(seRequest2);

        Assert.assertEquals(seResponseExpected.getApduResponses().get(0).getStatusCode(),
                seResponse1.getApduResponses().get(0).getStatusCode());

        Assert.assertThat(seResponseExpected.getApduResponses().get(0).getStatusCode(),
                IsNot.not(IsEqual.equalTo(seResponse1.getApduResponses().get(1).getStatusCode())));
    }
}
