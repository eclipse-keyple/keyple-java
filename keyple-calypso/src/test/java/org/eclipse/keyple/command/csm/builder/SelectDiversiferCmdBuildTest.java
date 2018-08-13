/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.command.csm.builder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.command.csm.builder.SelectDiversifierCmdBuild;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.GetDataFciCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.GetDataFciRespPars;
import org.eclipse.keyple.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exception.*;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.mockito.Mockito;

public class SelectDiversiferCmdBuildTest {

    private ByteBuffer dataIn;

    private ProxyReader fakeSpecificReader;

    private List<ApduRequest> ApduRequests = new ArrayList<ApduRequest>();

    private SeRequestSet seRequestSet;

    private List<ApduResponse> apduResponses;

    private SeResponseSet seResponseExpected;

    private byte[] returnOK = {(byte) 0x90, 0x00};

    private ApduResponse responseExpected = new ApduResponse(ByteBuffer.wrap(returnOK), null);

    private AbstractApduCommandBuilder apduCommandBuilder =
            new GetDataFciCmdBuild(PoRevision.REV3_1);

    private List<ApduResponse> list = new ArrayList<ApduResponse>();

    private List<ApduRequest> ApduRequests2 = new ArrayList<ApduRequest>();

    // TODO: rework this test and suppress SeRequest and SeResponse dependencies
    // Removed temporarily @Test
    public void selectDiversifier() throws IOReaderException, UnexpectedReaderException,
            ChannelStateReaderException, InvalidApduReaderException, ReaderTimeoutException,
            IllegalArgumentException, InconsistentParameterValueException {

        ApduRequest apdu = apduCommandBuilder.getApduRequest();
        ApduRequests.add(apdu);

        seRequestSet = new SeRequestSet(new SeRequest(null, ApduRequests, true));
        list.add(new ApduResponse(ByteBuffer
                .wrap(new byte[] {0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49,
                        0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08,
                        0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB9, 0x53, 0x07,
                        0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01, (byte) 0x90, 0x00}),
                null));
        list.add(new ApduResponse(ByteBuffer
                .wrap(new byte[] {0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49,
                        0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08,
                        0x00, 0x00, 0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB9, 0x53, 0x07,
                        0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01, (byte) 0x80, 0x00}),
                null));

        SeResponseSet seResponseSet = new SeResponseSet(new SeResponse(true, null, null, list));

        SeResponseSet responseFci = Mockito.mock(SeResponseSet.class);
        fakeSpecificReader = Mockito.mock(ProxyReader.class);

        Mockito.when(responseFci.getSingleResponse().getApduResponses()).thenReturn(list);
        Mockito.when(fakeSpecificReader.transmit(seRequestSet)).thenReturn(seResponseSet);

        GetDataFciRespPars.FCI fci = GetDataFciRespPars
                .toFCI(responseFci.getSingleResponse().getApduResponses().get(0).getBytes());
        dataIn = fci.getApplicationSN();

        AbstractApduCommandBuilder apduCommandBuilder2 =
                new SelectDiversifierCmdBuild(null, dataIn);
        ApduRequest ApduRequest = apduCommandBuilder2.getApduRequest();

        ApduRequests2.add(ApduRequest);

        apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseExpected);

        seResponseExpected =
                new SeResponseSet(new SeResponse(true, null, responseExpected, apduResponses));
        SeRequestSet seRequest2 = new SeRequestSet(new SeRequest(null, ApduRequests2, true));

        Mockito.when(fakeSpecificReader.transmit(seRequest2)).thenReturn(seResponseSet);
        SeResponseSet seResponse1 = fakeSpecificReader.transmit(seRequest2);

        Assert.assertEquals(
                seResponseExpected.getSingleResponse().getApduResponses().get(0).getStatusCode(),
                seResponse1.getSingleResponse().getApduResponses().get(0).getStatusCode());

        Assert.assertThat(
                seResponseExpected.getSingleResponse().getApduResponses().get(0).getStatusCode(),
                IsNot.not(IsEqual.equalTo(seResponse1.getSingleResponse().getApduResponses().get(1)
                        .getStatusCode())));
    }
}
