package org.keyple.plugin.smartcardio;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.keyple.plugin.smartcardio.SmartCardIOPlugin;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TerminalFactory.class })
public class SmartCardIOPluginTest {
    Logger logger = LoggerFactory.getLogger(SmartCardIOPluginTest.class);

    @Test
    public void testGetName() {
        SmartCardIOPlugin plugin = new SmartCardIOPlugin();
        assertEquals("SmartCardIOPlugin", plugin.getName());
    }

    @Test
    public void testGetReaders() throws CardException {

        TerminalFactory factory = PowerMockito.mock(TerminalFactory.class);
        SmartCardIOPlugin plugin = Mockito.mock(SmartCardIOPlugin.class);
        CardTerminals cardTerminals = Mockito.mock(CardTerminals.class);
        CardTerminal card = Mockito.mock(CardTerminal.class);
        List<CardTerminal> cards = new ArrayList<>();
        cards.add(card);
        when(factory.getType()).thenReturn("test");
        when(factory.terminals()).thenReturn(cardTerminals);
        when(cardTerminals.list()).thenReturn(cards);
        when(card.isCardPresent()).thenReturn(true);
        when(plugin.getTerminalFactory()).thenReturn(factory);

        logger.info(""+plugin.getReaders().size());
//        CardTerminal cardResult = plugin.getReaders()TerminalFactory().terminals().list().get(0);


    }

}
