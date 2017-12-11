package org.keyple.plugin.smartcardio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.TerminalFactory;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CardTerminal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class SmartCardIOPluginTest {
    Logger logger = LoggerFactory.getLogger(SmartCardIOPluginTest.class);

    @InjectMocks
    @Spy
    private SmartCardIOPlugin plugin;
    
    SmartCardIOPlugin smartCardPluginSpyied;
    
    @Mock
    CardTerminals cardTerminals;

    @Mock
    CardTerminal cardTerminal;
    
    @Before
    public void setUp() throws IOReaderException, CardException{
//        smartCardPluginSpyied = spy(plugin);
        when(plugin.getCardTerminals()).thenReturn(cardTerminals);
        List<CardTerminal> terms = new ArrayList<CardTerminal>();
        terms.add(cardTerminal);
        when(cardTerminals.list()).thenReturn(terms);
        when(cardTerminal.getName()).thenReturn("SmartCardIOPlugin");
    }
    
    @Test
    public void testGetReaders() throws CardException, IOReaderException {
        assertEquals(plugin.getReaders().size(), 1);
        assertEquals("SmartCardIOPlugin", plugin.getName());
    }
    
}
