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
package org.eclipse.keyple.plugin.pcsc;

import static org.junit.Assert.assertEquals;
import java.util.List;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SmartCardIOPluginTest {

    @InjectMocks
    @Spy
    private PcscPlugin plugin;

    PcscPlugin smartCardPluginSpyied;

    @Mock
    List<ProxyReader> cardTerminals;

    @Mock
    CardTerminal cardTerminal;

    // fclairamb (2018-03-01): This test made no sense, we were using a CardTerminals which is
    // PCSC-specific when we
    // should be returning ProxyReaders
    @Before
    public void setUp() throws KeypleReaderException, CardException {
        // smartCardPluginSpyied = spy(plugin);
        /*
         * when(plugin.getReaders()).thenReturn(cardTerminals); List<CardTerminal> terms = new
         * ArrayList<CardTerminal>(); terms.add(cardTerminal);
         * when(cardTerminals.list()).thenReturn(terms);
         * when(cardTerminal.getName()).thenReturn("PcscPlugin");
         */
    }

    @Test
    public void testGetReaders() throws KeypleReaderException {
        assertEquals(plugin.getReaders().size(), 1);
        assertEquals("PcscPlugin", plugin.getName());
    }

}
