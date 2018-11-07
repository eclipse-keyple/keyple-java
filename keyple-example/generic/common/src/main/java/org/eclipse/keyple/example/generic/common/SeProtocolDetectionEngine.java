/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.keyple.example.generic.common;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.transaction.MatchingSe;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelector;
import org.eclipse.keyple.util.ByteArrayUtils;

/**
 * This code demonstrates the multi-protocols capability of the Keyple SeProxy
 * <ul>
 * <li>instantiates a PC/SC plugin for a reader which name matches the regular expression provided
 * by poReaderName.</li>
 * <li>uses the observable mechanism to handle SE insertion/detection</li>
 * <li>expects SE with various protocols (technologies)</li>
 * <li>shows the identified protocol when a SE is detected</li>
 * <li>executes a simple Hoplink reading when a Hoplink SE is identified</li>
 * </ul>
 * The program spends most of its time waiting for a Enter key before exit. The actual SE processing
 * is mainly event driven through the observability.
 */
public class SeProtocolDetectionEngine extends AbstractTransactionEngine
        implements ObservableReader.ReaderObserver {
    private ProxyReader poReader;

    public SeProtocolDetectionEngine() {
        super();
    }

    /* Assign reader to the transaction engine */
    public void setReader(ProxyReader poReader) {
        this.poReader = poReader;
    }

    public void initialize() {

    }

    /**
     * This method is called when a SE is inserted (or presented to the reader's antenna). It
     * executes a SeRequestSet and processes the SeResponseSet showing the APDUs exchanges
     */
    public void operateSeTransaction() {
        try {
            SeSelection seSelection = new SeSelection(poReader);

            ApduRequest pcscContactlessReaderGetData =
                    new ApduRequest(ByteArrayUtils.fromHex("FFCA000000"), false);
            List<ApduRequest> pcscContactlessReaderGetDataList = new ArrayList<ApduRequest>();
            pcscContactlessReaderGetDataList.add(pcscContactlessReaderGetData);

            // process SDK defined protocols
            for (ContactlessProtocols protocol : ContactlessProtocols.values()) {
                switch (protocol) {
                    case PROTOCOL_ISO14443_4:
                        /* Add a Hoplink selector */
                        String HoplinkAID = "A000000291A000000191";
                        byte SFI_T2Usage = (byte) 0x1A;
                        byte SFI_T2Environment = (byte) 0x14;

                        PoSelector poSelector = new PoSelector(
                                new SeSelector.SelectionParameters(
                                        ByteArrayUtils.fromHex(HoplinkAID), false),
                                true, ContactlessProtocols.PROTOCOL_ISO14443_4,
                                PoSelector.RevisionTarget.TARGET_REV3, "Hoplink selector");

                        poSelector.preparePoCustomReadCmd("Standard Get Data",
                                new ApduRequest(ByteArrayUtils.fromHex("FFCA000000"), false));

                        poSelector.prepareReadRecordsCmd(SFI_T2Environment, (byte) 0x01, true,
                                (byte) 0x00, "Hoplink T2 Environment");

                        seSelection.prepareSelector(poSelector);

                        break;
                    case PROTOCOL_ISO14443_3A:
                    case PROTOCOL_ISO14443_3B:
                        // not handled in this demo code
                        break;
                    case PROTOCOL_MIFARE_DESFIRE:
                    case PROTOCOL_B_PRIME:
                        // intentionally ignored for demo purpose
                        break;
                    default:
                        /* Add a generic selector */
                        seSelection.prepareSelector(new SeSelector(
                                new SeSelector.SelectionParameters(".*", null), true,
                                ContactlessProtocols.PROTOCOL_ISO14443_4, "Default selector"));
                        break;
                }
            }

            seSelection.processSelection();

            for (MatchingSe matchingSe : seSelection.getMatchingSeList()) {
                System.out.println("Selector: " + matchingSe.getExtraInfo()
                        + ", selection status = " + matchingSe.isSelected());
            }

        } catch (KeypleReaderException ex) {
            System.out.println("Selection exception: " + ex.getCause());
        }
    }
}
