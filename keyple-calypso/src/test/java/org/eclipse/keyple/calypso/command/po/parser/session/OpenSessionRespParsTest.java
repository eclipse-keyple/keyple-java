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

package org.eclipse.keyple.calypso.command.po.parser.session;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.util.TestsUtilsResponseTabByteGenerator;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OpenSessionRespParsTest {

    private void check(AbstractOpenSessionRespPars resp) {
        Assert.assertTrue(resp.isSuccessful());
    }

    @Test
    public void testgetResponse_rev2_4() {

        // expected response

        ApduResponse responseMockFci =
                TestsUtilsResponseTabByteGenerator.generateApduResponseValidRev2_4();
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseMockFci);

        SeResponseSet responseMock =
                new SeResponseSet(new SeResponse(true, null, responseMockFci, apduResponses));
        ApduResponse response = responseMock.getSingleResponse().getApduResponses().get(0);

        check(AbstractOpenSessionRespPars.create(response, PoRevision.REV2_4));
    }

    @Test
    public void testgetResponse_rev3_1() {

        // expected response

        ApduResponse responseMockFci =
                TestsUtilsResponseTabByteGenerator.generateApduResponseValidRev3_1();
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseMockFci);

        SeResponseSet responseMock =
                new SeResponseSet(new SeResponse(true, null, responseMockFci, apduResponses));
        ApduResponse response = responseMock.getSingleResponse().getApduResponses().get(0);

        check(AbstractOpenSessionRespPars.create(response, PoRevision.REV3_1));
    }

    @Test
    public void testgetResponse_rev3_2() {

        // expected response

        ApduResponse responseMockOS =
                TestsUtilsResponseTabByteGenerator.generateApduResponseValidRev3_2();
        ApduResponse responseMockFci =
                TestsUtilsResponseTabByteGenerator.generateApduResponseValidRev3_2();
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseMockOS);

        SeResponseSet responseMock =
                new SeResponseSet(new SeResponse(true, null, responseMockFci, apduResponses));
        ApduResponse response = responseMock.getSingleResponse().getApduResponses().get(0);

        check(AbstractOpenSessionRespPars.create(response, PoRevision.REV3_2));
    }

}
