package org.keyple.plugin.android.omapi;

import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.SeRequest;
import org.keyple.seproxy.SeResponse;
import org.keyple.seproxy.exceptions.IOReaderException;


public class AndroidOmapiReader implements ProxyReader {


    @Override
    public String getName() {
        return null;
    }

    @Override
    public SeResponse transmit(SeRequest seApplicationRequest) throws IOReaderException {
        return null;
    }

    @Override
    public boolean isSEPresent() throws IOReaderException {
        return false;
    }
}
