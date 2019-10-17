package org.eclipse.keyple.core.seproxy.plugin;

import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;

import java.util.Map;

public class BlankSmartSelectionReader extends AbstractLocalReader implements SmartSelectionReader {


    BlankSmartSelectionReader(String pluginName, String readerName) {
        super(pluginName, readerName);
    }

    @Override
    protected boolean checkSePresence() throws NoStackTraceThrowable {
        return false;
    }

    @Override
    protected byte[] getATR() {
        return new byte[0];
    }

    @Override
    protected void openPhysicalChannel() throws KeypleChannelControlException {

    }

    @Override
    protected void closePhysicalChannel() throws KeypleChannelControlException {

    }

    @Override
    protected boolean isPhysicalChannelOpen() {
        return false;
    }

    @Override
    protected boolean protocolFlagMatches(SeProtocol protocolFlag) throws KeypleReaderException {
        return false;
    }

    @Override
    protected byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException {
        return new byte[0];
    }

    @Override
    public ApduResponse openChannelForAid(SeSelector.AidSelector aidSelector) throws KeypleIOReaderException, KeypleChannelControlException, KeypleApplicationSelectionException {
        return null;
    }

    @Override
    public TransmissionMode getTransmissionMode() {
        return null;
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IllegalArgumentException, KeypleBaseException {

    }
}
