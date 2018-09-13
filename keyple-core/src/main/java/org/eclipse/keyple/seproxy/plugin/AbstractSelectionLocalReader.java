/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.plugin;

import java.nio.ByteBuffer;
import java.util.Set;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleApplicationSelectionException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings({"PMD.ModifiedCyclomaticComplexity", "PMD.CyclomaticComplexity",
        "PMD.StdCyclomaticComplexity"})
/**
 * Local reader class implementing the logical channel opening based on the selection of the SE
 * application
 */
public abstract class AbstractSelectionLocalReader extends AbstractLocalReader
        implements ObservableReader {
    private static final Logger logger =
            LoggerFactory.getLogger(AbstractSelectionLocalReader.class);

    protected AbstractSelectionLocalReader(String pluginName, String readerName) {
        super(pluginName, readerName);
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
     * @throws KeypleReaderException if a reader error occurs
     * @throws KeypleReaderException if the channel opening fails
     */
    protected abstract void openPhysicalChannel()
            throws KeypleReaderException, KeypleReaderException;

    /**
     * Opens a logical channel
     * 
     * @param selector the SE Selector: AID of the application to select or ATR regex
     * @param successfulSelectionStatusCodes the list of successful status code for the select
     *        command
     * @return 2 ByteBuffers: ATR and FCI data
     * @throws KeypleReaderException - if an IO exception occurred
     * @throws KeypleApplicationSelectionException - if the application selection is not successful
     */
    protected final ByteBuffer[] openLogicalChannelAndSelect(SeRequest.Selector selector,
            Set<Short> successfulSelectionStatusCodes)
            throws KeypleReaderException, KeypleApplicationSelectionException {
        ByteBuffer[] atrAndFci = new ByteBuffer[2];

        if (!isLogicalChannelOpen()) {
            /*
             * init of the physical SE channel: if not yet established, opening of a new physical
             * channel
             */
            if (!isPhysicalChannelOpen()) {
                openPhysicalChannel();
            }
            if (!isPhysicalChannelOpen()) {
                throw new KeypleReaderException("Fail to open physical channel.");
            }
        }

        /* add ATR */
        atrAndFci[0] = getATR();
        if (logger.isTraceEnabled()) {
            logger.trace("[{}] openLogicalChannelAndSelect => ATR: {}", this.getName(),
                    ByteBufferUtils.toHex(atrAndFci[0]));
        }
        /* selector may be null, in this case we consider the logical channel open */
        if (selector != null) {
            if (selector instanceof SeRequest.AidSelector) {
                ByteBuffer aid = ((SeRequest.AidSelector) selector).getAidToSelect();
                if (aid != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace(
                                "[{}] openLogicalChannelAndSelect => Select Application with AID = {}",
                                this.getName(), ByteBufferUtils.toHex(aid));
                    }
                    /*
                     * build a get response command the actual length expected by the SE in the get
                     * response command is handled in transmitApdu
                     */
                    ByteBuffer selectApplicationCommand = ByteBuffer.allocate(6 + aid.limit());
                    selectApplicationCommand.put((byte) 0x00); // CLA
                    selectApplicationCommand.put((byte) 0xA4); // INS
                    selectApplicationCommand.put((byte) 0x04); // P1
                    selectApplicationCommand.put((byte) 0x00); // P2
                    selectApplicationCommand.put((byte) (aid.limit())); // Lc
                    selectApplicationCommand.put(aid); // data
                    selectApplicationCommand.put((byte) 0x00); // Le
                    selectApplicationCommand.position(0);

                    /*
                     * we use here processApduRequest to manage case 4 hack the successful status
                     * codes list for this command is provided
                     */
                    ApduResponse fciResponse =
                            processApduRequest(new ApduRequest(selectApplicationCommand, true,
                                    successfulSelectionStatusCodes)
                                            .setName("Intrinsic Select Application"));

                    /* add FCI */
                    atrAndFci[1] = fciResponse.getBytes();

                    if (!fciResponse.isSuccessful()) {
                        logger.trace(
                                "[{}] openLogicalChannelAndSelect => Application Selection failed. SELECTOR = {}",
                                this.getName(), selector);
                        throw new KeypleApplicationSelectionException(
                                "Application selection by AID failed " + selector.toString());
                    }
                }
            } else {
                if (!((SeRequest.AtrSelector) selector).atrMatches(atrAndFci[0])) {
                    logger.trace(
                            "[{}] openLogicalChannelAndSelect => ATR Selection failed. SELECTOR = {}",
                            this.getName(), selector);
                    throw new KeypleApplicationSelectionException(
                            "Application selection by ATR failed " + selector.toString());
                }
            }
        }
        return atrAndFci;
    }

    public final void addObserver(ReaderObserver observer) {
        super.addObserver(observer);
    }

    public final void removeObserver(ReaderObserver observer) {
        super.removeObserver(observer);
    }
}
