package cna.sdk.plugin.smartcardio;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.seproxy.ProxyReader;
import cna.sdk.seproxy.ReadersPlugin;

@SuppressWarnings("restriction")
public class SmartCardIOPlugin implements ReadersPlugin {

    static final Logger logger = LoggerFactory.getLogger(SmartCardIOPlugin.class);

    private TerminalFactory factory = TerminalFactory.getDefault();

    @Override
    public String getName() {
        return "SmartCardIOPlugin";
    }

    @Override
    public List<ProxyReader> getReaders() {
        List<ProxyReader> readers = new ArrayList<>();
        // connection avec un terminal

        CardTerminals terminals = getTerminalFactory().terminals();
        try {
            for (CardTerminal terminal : terminals.list()) {
                readers.add(new SmartCardIOReader(terminal, terminal.getName()));
            }
        } catch (CardException | NullPointerException e) {
            logger.error("Terminal List no accessible", e);
        }
        return readers;
    }

    public TerminalFactory getTerminalFactory() {
        return this.factory;
    }

}
