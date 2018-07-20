package org.eclipse.keyple.plugin.stub;

import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;

import java.nio.ByteBuffer;

public interface StubSecureElement {

    ByteBuffer getATR();
    boolean isPhysicalChannelOpen();
    void openPhysicalChannel() throws IOReaderException, ChannelStateReaderException ;
    void closePhysicalChannel()throws IOReaderException;
    ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException;
    SeProtocol getSeProcotol();

}
