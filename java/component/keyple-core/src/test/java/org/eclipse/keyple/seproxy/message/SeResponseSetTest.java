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

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
@RunWith(MockitoJUnitRunner.class)
public class SeResponseSetTest {

    @Test
    public void getSingleResponse() throws Exception {
        SeResponseSet set = new SeResponseSet(SeResponseTest.getASeResponse());
        // assertArrayEquals(SeResponseTest.getASeResponse().getApduResponses().toArray(),
        // set.getSingleResponse().getApduResponses().toArray());
        assertEquals(SeResponseTest.getASeResponse(), set.getSingleResponse());
    }

    @Test(expected = IllegalStateException.class)
    public void getSingleResponseFail() throws Exception {
        List<SeResponse> seResponses = new ArrayList<SeResponse>();
        seResponses.add(SeResponseTest.getASeResponse());
        seResponses.add(SeResponseTest.getASeResponse());
        SeResponseSet set = new SeResponseSet(seResponses);

        set.getSingleResponse();// throw exception
    }

    @Test
    public void getResponses() throws Exception {
        List<SeResponse> seResponses = new ArrayList<SeResponse>();
        seResponses.add(SeResponseTest.getASeResponse());
        seResponses.add(SeResponseTest.getASeResponse());
        SeResponseSet set = new SeResponseSet(seResponses);
        assertArrayEquals(seResponses.toArray(), set.getResponses().toArray());
    }

    @Test
    public void toStringTest() throws Exception {
        SeResponse emptySeR = new SeResponse(true, new SelectionStatus(
                new AnswerToReset(ByteArrayUtils.fromHex("3BFF00")), null, true), null);
        SeResponseSet set = new SeResponseSet(emptySeR);
        assertNotNull(set.toString());
    }
}
