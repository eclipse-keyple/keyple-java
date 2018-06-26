/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.local;

import java.nio.ByteBuffer;
import java.util.Set;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.SelectApplicationException;
import org.eclipse.keyple.util.ByteBufferUtils;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

/**
 * Local reader class implementing the logical channel opening based on the selection of the SE
 * application
 */
public abstract class AbstractSelectionLocalReader extends AbstractLocalReader {
    private static final ILogger logger =
            SLoggerFactory.getLogger(AbstractSelectionLocalReader.class);

    public AbstractSelectionLocalReader(String name) {
        super(name);
    }

    /**
     * Gets the SE Answer to reset
     *
     * @return ATR returned by the SE or reconstructed by the reader (contactless)
     */
    protected abstract ByteBuffer getATR();

    /**
     * Tells if the physical channel is open or not
     *
     * @return true is the channel is open
     */
    protected abstract boolean isPhysicalChannelOpen();

    /**
     * Attempts to open the physical channel
     *
     * @throws IOReaderException
     * @throws ChannelStateReaderException
     */
    protected abstract void openPhysicalChannel()
            throws IOReaderException, ChannelStateReaderException;

    /**
     * Opens a logical channel
     * 
     * @param aid the AID of the application to select
     * @param successfulSelectionStatusCodes the list of successful status code for the select
     *        command
     * @return 2 ByteBuffers: ATR and FCI data
     * @throws IOReaderException
     * @throws SelectApplicationException
     */
    protected final ByteBuffer[] openLogicalChannelAndSelect(ByteBuffer aid,
            Set<Short> successfulSelectionStatusCodes)
            throws IOReaderException, SelectApplicationException {
        ByteBuffer[] atrAndFci = new ByteBuffer[2];

        if (!isLogicalChannelOpen()) {
            // init of the physical SE channel: if not yet established, opening of a new physical
            // channel
            if (!isPhysicalChannelOpen()) {
                openPhysicalChannel();
            }
            if (!isPhysicalChannelOpen()) {
                throw new ChannelStateReaderException("Fail to open physical channel.");
            }
        }

        // add ATR
        atrAndFci[0] = getATR();
        if (aid != null) {
            logger.info("Connecting to card", "action", "local_reader.openLogicalChannel", "aid",
                    ByteBufferUtils.toHex(aid), "readerName", getName());
            try {
                // build a get response command
                // the actual length expected by the SE in the get response command is handled in
                // transmitApdu
                ByteBuffer selectApplicationCommand = ByteBufferUtils
                        .fromHex("00A40400" + String.format("%02X", (byte) aid.limit())
                                + ByteBufferUtils.toHex(aid) + "00");

                // we use here processApduRequest to manage case 4 hack
                // the successful status codes list for this command is provided
                ApduResponse fciResponse =
                        processApduRequest(new ApduRequest(selectApplicationCommand, true,
                                successfulSelectionStatusCodes));

                // add FCI
                atrAndFci[1] = fciResponse.getBytes();

                if (!fciResponse.isSuccessful()) {
                    logger.info("Application selection failed", "action",
                            "pcsc_reader.openLogicalChannel", "aid", ByteBufferUtils.toHex(aid),
                            "fci", ByteBufferUtils.toHex(fciResponse.getBytes()));
                    throw new SelectApplicationException("Application selection failed");
                }
            } catch (ChannelStateReaderException e1) {
                throw new ChannelStateReaderException(e1);
            }
        }
        return atrAndFci;
    }
}
