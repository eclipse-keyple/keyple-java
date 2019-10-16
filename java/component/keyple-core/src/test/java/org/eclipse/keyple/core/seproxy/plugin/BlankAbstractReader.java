package org.eclipse.keyple.core.seproxy.plugin;

import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A blank class extending AbstractReader
 * only purpose is to be tested and spied by mockito
 */
public class BlankAbstractReader extends AbstractReader {

    BlankAbstractReader(String pluginName, String readerName){
        super(pluginName,readerName);
    }

    @Override
    protected List<SeResponse> processSeRequestSet(Set<SeRequest> requestSet, MultiSeRequestProcessing multiSeRequestProcessing, ChannelControl channelControl) throws KeypleReaderException {
        return null;
    }

    @Override
    protected SeResponse processSeRequest(SeRequest seRequest, ChannelControl channelControl) throws KeypleReaderException {
        return null;
    }

    @Override
    public boolean isSePresent() throws NoStackTraceThrowable {
        return false;
    }

    @Override
    public void addSeProtocolSetting(SeProtocol seProtocol, String protocolRule) {

    }

    @Override
    public void setSeProtocolSetting(Map<SeProtocol, String> protocolSetting) {

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
