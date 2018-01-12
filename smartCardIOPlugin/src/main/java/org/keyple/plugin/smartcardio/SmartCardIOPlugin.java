package org.keyple.plugin.smartcardio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.ReadersPlugin;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartCardIOPlugin implements ReadersPlugin {
  
    static final Logger logger = LoggerFactory.getLogger(SmartCardIOPlugin.class);

    private TerminalFactory factory = TerminalFactory.getDefault();

    private Map<String, ProxyReader> readers;
    
    
    public SmartCardIOPlugin(){
        this.readers = new HashMap<String, ProxyReader>();
    }

    
    @Override
    public String getName() {
        return "SmartCardIOPlugin";
    }

    @Override
    public List<ProxyReader> getReaders() throws IOReaderException {
        CardTerminals terminals = getCardTerminals();
        
        if(terminals == null){
            logger.error("Not terminals found", new Throwable());
            throw new IOReaderException("Not terminals found", new Throwable());
        }
        try {
            if(this.readers.isEmpty()){
                for (CardTerminal terminal : terminals.list()) {
                    SmartCardIOReader reader = new SmartCardIOReader(terminal, terminal.getName());
                    if(!this.readers.containsKey(reader.getName())){
                        this.readers.put(reader.getName(), reader);
                    }
                }
            }
        } catch (CardException e) {
            logger.error("Terminal List not accessible", e);
            throw new IOReaderException(e.getMessage(), e);
        } catch (NullPointerException e) {
            logger.error("Terminal List not accessible", e);
            throw new IOReaderException(e.getMessage(), e);
        }
        
        return new ArrayList<ProxyReader>(this.readers.values());
    }

    public CardTerminals getCardTerminals() {
        return this.factory.terminals();
    }

}
