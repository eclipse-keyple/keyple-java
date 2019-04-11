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
package org.eclipse.keyple.example.generic.common;



import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.DefaultSelectionRequest;
import org.eclipse.keyple.seproxy.event.SelectionResponse;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.transaction.*;
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
public class SeProtocolDetectionEngine extends AbstractReaderObserverEngine {
    private SeReader poReader;
    private SeSelection seSelection;

    public SeProtocolDetectionEngine() {
        super();
    }

    /* Assign reader to the transaction engine */
    public void setReader(SeReader poReader) {
        this.poReader = poReader;
    }

    public DefaultSelectionRequest prepareSeSelection() {

        seSelection = new SeSelection();

        // process SDK defined protocols
        for (ContactlessProtocols protocol : ContactlessProtocols.values()) {
            switch (protocol) {
                case PROTOCOL_ISO14443_4:
                    /* Add a Hoplink selector */
                    String HoplinkAID = "A000000291A000000191";
                    byte SFI_T2Usage = (byte) 0x1A;
                    byte SFI_T2Environment = (byte) 0x14;

                    PoSelectionRequest poSelectionRequest =
                            new PoSelectionRequest(
                                    new SeSelector(
                                            new SeSelector.AidSelector(
                                                    ByteArrayUtils.fromHex(HoplinkAID), null),
                                            null, "Hoplink selector"),
                                    ChannelState.KEEP_OPEN,
                                    ContactlessProtocols.PROTOCOL_ISO14443_4);

                    poSelectionRequest.preparePoCustomReadCmd("Standard Get Data",
                            new ApduRequest(ByteArrayUtils.fromHex("FFCA000000"), false));

                    poSelectionRequest.prepareReadRecordsCmd(SFI_T2Environment,
                            ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01,
                            "Hoplink T2 Environment");

                    seSelection.prepareSelection(poSelectionRequest);

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
                    seSelection.prepareSelection(new SeSelectionRequest(
                            new SeSelector(null, new SeSelector.AtrFilter(".*"),
                                    "Default selector"),
                            ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4));
                    break;
            }
        }
        return seSelection.getSelectionOperation();
    }

    /**
     * This method is called when a SE is inserted (or presented to the reader's antenna). It
     * executes a {@link DefaultSelectionRequest} and processes the {@link SelectionResponse}
     * showing the APDUs exchanges
     */
    @Override
    public void processSeMatch(SelectionResponse selectionResponse) {
        SelectionsResult selectionsResult = seSelection.processDefaultSelection(selectionResponse);
        /* get the SE that matches one of the two selection targets */
        MatchingSe selectedSe = selectionsResult.getActiveSelection().getMatchingSe();
        if (selectedSe != null) {
            System.out.println("Selector: " + selectedSe.getSelectionExtraInfo()
                    + ", selection status = " + selectedSe.isSelected());
        } else {
            System.out.println("No selection matched!");
        }
    }

    @Override
    public void processSeInsertion() {
        System.out.println("Unexpected SE insertion event");
    }

    @Override
    public void processSeRemoval() {
        System.out.println("SE removal event");
    }

    @Override
    public void processUnexpectedSeRemoval() {
        System.out.println("Unexpected SE removal event");
    }
}
