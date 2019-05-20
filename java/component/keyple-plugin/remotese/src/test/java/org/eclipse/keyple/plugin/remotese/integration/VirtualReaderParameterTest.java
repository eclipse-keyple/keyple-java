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
package org.eclipse.keyple.plugin.remotese.integration;



import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Virtual Reader Parameters with stub plugin and hoplink SE
 */
public class VirtualReaderParameterTest extends VirtualReaderBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(VirtualReaderParameterTest.class);

    @Before
    public void setUp() throws Exception {
        // restore plugin state
        clearStubpluginReaders();

        initKeypleServices();
    }

    @After
    public void tearDown() throws Exception {
        clearStubpluginReaders();
    }

    @Test
    public void testGetTransmissionMode() throws Exception {
        // configure and connect a Stub Native reader
        nativeReader = this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID,
                TransmissionMode.CONTACTLESS);

        // test virtual reader
        virtualReader = getVirtualReader();

        Assert.assertEquals(virtualReader.getTransmissionMode(),
                nativeReader.getTransmissionMode());
    }

}
