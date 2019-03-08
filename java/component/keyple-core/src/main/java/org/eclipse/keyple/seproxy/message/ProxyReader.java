/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.seproxy.message;


import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;


/**
 * ProxyReader interface
 * <ul>
 * <li>To operate the transmission of SeRequestSet, a specific local reader processes the sorted
 * list of SeRequest.</li>
 * <li>According to SeRequest protocolFlag and to the current status of the reader (RF protocol
 * involved / current ATR) the processing of a specific SeRequest could be skipped.</li>
 * <li>When processing a SeRequest</li>
 * <li>if necessary a new logical channel is open (for a specific AID if defined)</li>
 * <li>and ApduRequest are transmited one by one</li>
 * </ul>
 * This interface should be implemented by any specific reader plugin.
 */
public interface ProxyReader extends SeReader {

    /**
     * Transmits a {@link SeRequestSet} (list of {@link SeRequest}) to a SE application and get back
     * the corresponding {@link SeResponseSet} (list of {@link SeResponse}).
     * <p>
     * The usage of this method is conditioned to the presence of a SE in the selected reader.
     * <p>
     * All the {@link SeRequest} are processed consecutively. The received {@link SeResponse} and
     * placed in the {@link SeResponseSet}.
     * <p>
     * If the protocol flag set in the request match the current SE protocol and the keepChannelOpen
     * flag is set to true, the transmit method returns immediately with a {@link SeResponseSet}.
     * This response contains the received response from the matching SE in the last position of
     * set. The previous one are set to null, the logical channel is open.
     * <p>
     * If the protocol flag set in the request match the current SE protocol and the keepChannelOpen
     * flag is set to false, the transmission go on for the next {@link SeRequest}. The channel is
     * left closed.
     * <p>
     * This method could also fail in case of IO error or wrong card state &rarr; some reader’s
     * exception (SE missing, IO error, wrong card state, timeout) have to be caught during the
     * processing of the SE request transmission.
     *
     * @param seApplicationRequest the application request
     * @return the SE response
     * @throws KeypleReaderException An error occurs during transmit (channel, IO)
     */
    SeResponseSet transmitSet(SeRequestSet seApplicationRequest)
            throws KeypleReaderException, IllegalArgumentException;

    /**
     * Transmits a single {@link SeRequest} (list of {@link ApduRequest}) and get back the
     * corresponding {@link SeResponse}
     * <p>
     * The usage of this method is conditioned to the presence of a SE in the selected reader.
     * <p>
     * The {@link SeRequest} is processed and the received {@link SeResponse} is returned.
     * <p>
     * The logical channel is set according to the keepChannelOpen flag.
     *
     * <p>
     * This method could also fail in case of IO error or wrong card state &rarr; some reader’s
     * exception (SE missing, IO error, wrong card state, timeout) have to be caught during the
     * processing of the SE request transmission. *
     * 
     * @param seApplicationRequest the SeRequest to transmit
     * @return SeResponse the response to the SeRequest
     * @throws KeypleReaderException in case of a reader exception
     * @throws IllegalArgumentException if a bad argument is provided
     */
    SeResponse transmit(SeRequest seApplicationRequest)
            throws KeypleReaderException, IllegalArgumentException;
}
