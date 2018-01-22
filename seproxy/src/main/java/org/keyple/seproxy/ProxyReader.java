package org.keyple.seproxy;

import org.keyple.seproxy.exceptions.ChannelStateReaderException;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.seproxy.exceptions.InvalidApduReaderException;
import org.keyple.seproxy.exceptions.TimeoutReaderException;
import org.keyple.seproxy.exceptions.UnexpectedReaderException;

/**
 * The Interface ProxyReader. This interface has to be implemented by each
 * plugins of readers’ drivers.
 *
 * @author Ixxi
 */
public interface ProxyReader {

    /**
     * Gets the name.
     *
     * @return returns the ‘unique’ name of the SE reader for the selected
     *         plugin.
     */
    String getName();

    /**
     * Transmits a request to a SE application and get back the corresponding SE
     * response o the usage of this method is conditioned to the presence of a
     * SE in the selected reader, this method could also fail in case of IO
     * error or wrong card state → some reader’s exceptions (SE missing, IO
     * error, wrong card state, timeout) have to be caught during the processing
     * of the SE request transmission.
     *
     * @param seApplicationRequest
     *            the se application request
     * @return the SE response
     * @throws ChannelStateReaderException
     *             Exception of type Channel State Reader
     * @throws InvalidApduReaderException
     *             Exception of type Invalid APDU
     * @throws IOReaderException
     *             Exception of type IO Reader
     * @throws TimeoutReaderException
     *             Exception of type timeout
     * @throws UnexpectedReaderException
     *             Unexepected exception
     */
    SeResponse transmit(SeRequest seApplicationRequest) throws ChannelStateReaderException, InvalidApduReaderException,
            IOReaderException, TimeoutReaderException, UnexpectedReaderException;

    /**
     * Checks if is SE present.
     *
     * @return true if a Secure Element is present in the reader
     * @throws IOReaderException
     *             Exception of type IO Reader
     */
    boolean isSEPresent() throws IOReaderException;
}
