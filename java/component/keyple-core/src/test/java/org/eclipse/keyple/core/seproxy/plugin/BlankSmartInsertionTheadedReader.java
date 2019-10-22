package org.eclipse.keyple.core.seproxy.plugin;

import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;

public class BlankSmartInsertionTheadedReader extends AbstractThreadedObservableLocalReader implements SmartInsertionReader {

    private static final Logger logger = LoggerFactory.getLogger(BlankSmartInsertionTheadedReader.class);

    /**
     * Reader constructor
     * <p>
     * Force the definition of a name through the use of super method.
     * <p>
     *
     * @param pluginName the name of the plugin that instantiated the reader
     * @param readerName the name of the reader
     */
    public BlankSmartInsertionTheadedReader(String pluginName, String readerName) {
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

    /*
    @Override
    public boolean waitForCardPresent(long timeout) {
        // Obtain a number between [0 - 49].
        int n = new Random().nextInt(10);
        boolean isCardPresent =  (n==2);
        logger.trace("is card present {}",isCardPresent);
        return isCardPresent;
    }
    */

    @Override
    public boolean waitForCardPresent(long timeout) {
        return false;
    }
}
