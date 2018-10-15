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

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SeRequestSetTest {

    // object to test
    SeRequestSet seRequestSet;

    // attributes
    Set<SeRequest> sortedRequests;

    SeRequest firstRequest;


    @Before
    public void setUp() throws Exception {
        // init
        sortedRequests = new HashSet<SeRequest>();

        firstRequest = SeRequestTest.getSeRequestSample();
        sortedRequests.add(firstRequest);

        // init
        seRequestSet = new SeRequestSet(sortedRequests);
    }

    @Test
    public void getRequests() {
        assertArrayEquals(sortedRequests.toArray(), seRequestSet.getRequests().toArray());
    }

    @Test
    public void getSingleRequest() {
        seRequestSet = new SeRequestSet(firstRequest);

        // with only one element it works
        assertEquals(firstRequest, seRequestSet.getSingleRequest());
    }

    @Test(expected = IllegalStateException.class)
    public void getSingleRequestFail() {
        // put a second element
        sortedRequests.add(SeRequestTest.getSeRequestSample());
        seRequestSet = new SeRequestSet(sortedRequests);
        seRequestSet.getSingleRequest();// raise exception because it works only with one element
    }

    @Test
    public void toStringNull() {
        seRequestSet = new SeRequestSet(new HashSet<SeRequest>());
        assertNotNull(seRequestSet.toString());
    }

    /*
     * CONSTRUCTOR
     */


}
