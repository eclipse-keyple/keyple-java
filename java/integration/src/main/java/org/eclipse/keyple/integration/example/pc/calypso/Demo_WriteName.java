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
package org.eclipse.keyple.integration.example.pc.calypso;



import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.integration.calypso.PoFileStructureInfo;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.transaction.*;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.VariableNamingConventions")
public class Demo_WriteName {

    private static final Logger logger = LoggerFactory.getLogger(Demo_WriteName.class);

    public static void main(String[] args) throws KeypleBaseException, NoStackTraceThrowable {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        SeReader poReader =
                DemoUtilities.getReader(seProxyService, DemoUtilities.PO_READER_NAME_REGEX);
        SeReader samReader =
                DemoUtilities.getReader(seProxyService, DemoUtilities.SAM_READER_NAME_REGEX);

        /* Check if the readers exists */
        if (poReader == null || samReader == null) {
            throw new IllegalStateException("Bad PO or SAM reader setup");
        }

        logger.info("= PO Reader  NAME = {}", poReader.getName());
        logger.info("= SAM Reader  NAME = {}", samReader.getName());

        String SAM_ATR_REGEX = "3B3F9600805A[0-9a-fA-F]{2}80[0-9a-fA-F]{16}829000";

        SeSelection samSelection = new SeSelection();

        SeSelectionRequest samSelectionRequest = new SeSelectionRequest(
                new SeSelector(null, new SeSelector.AtrFilter(SAM_ATR_REGEX), "SAM Selection"),
                ChannelState.KEEP_OPEN, Protocol.ANY);

        /* Prepare selector, ignore MatchingSe here */
        samSelection.prepareSelection(samSelectionRequest);

        try {
            if (!samSelection.processExplicitSelection(samReader).hasActiveSelection()) {
                System.out.println("Unable to open a logical channel for SAM!");
                throw new IllegalStateException("SAM channel opening failure");
            }
        } catch (KeypleReaderException e) {
            throw new IllegalStateException("Reader exception: " + e.getMessage());

        }

        /* Check if a PO is present in the reader */
        if (poReader.isSePresent()) {

            /*
             * Prepare a Calypso PO selection
             */
            SeSelection seSelection = new SeSelection();

            /*
             * Setting of an AID based selection of a Calypso REV3 PO
             *
             * Select the first application matching the selection AID whatever the SE communication
             * protocol keep the logical channel open after the selection
             */

            /*
             * Calypso selection: configures a PoSelectionRequest with all the desired attributes to
             * make the selection and read additional information afterwards
             */
            /* Calypso AID */
            String poAuditC0Aid = "315449432E4943414C54"; // AID of the PO with Audit C0 profile
            String clapAid = "315449432E494341D62010029101"; // AID of the CLAP product being tested
            String cdLightAid = "315449432E494341"; // AID of the Rev2.4 PO emulating CDLight

            // Add Audit C0 AID to the list
            int auditC0SeIndex =
                    seSelection
                            .prepareSelection(
                                    new PoSelectionRequest(
                                            new SeSelector(new SeSelector.AidSelector(
                                                    ByteArrayUtils.fromHex(
                                                            PoFileStructureInfo.poAuditC0Aid),
                                                    null), null, "Audit C0"),
                                            ChannelState.KEEP_OPEN, Protocol.ANY));

            // Add CLAP AID to the list
            int clapSe =
                    seSelection
                            .prepareSelection(
                                    new PoSelectionRequest(
                                            new SeSelector(
                                                    new SeSelector.AidSelector(
                                                            ByteArrayUtils.fromHex(
                                                                    PoFileStructureInfo.clapAid),
                                                            null),
                                                    null, "CLAP"),
                                            ChannelState.KEEP_OPEN, Protocol.ANY));

            // Add cdLight AID to the list
            int cdLightSe =
                    seSelection
                            .prepareSelection(new PoSelectionRequest(
                                    new SeSelector(
                                            new SeSelector.AidSelector(ByteArrayUtils
                                                    .fromHex(PoFileStructureInfo.cdLightAid), null),
                                            null, "CDLight"),
                                    ChannelState.KEEP_OPEN, Protocol.ANY));

            SelectionsResult selectionsResult = seSelection.processExplicitSelection(poReader);
            if (!selectionsResult.hasActiveSelection()) {
                throw new IllegalArgumentException("No recognizable PO detected.");
            }

            byte environmentSid = (byte) 0x00;
            int activeSelectionIndex = selectionsResult.getActiveSelection().getSelectionIndex();
            if (auditC0SeIndex == auditC0SeIndex) {
                environmentSid = (byte) 0x07;
            } else if (auditC0SeIndex == clapSe) {
                environmentSid = (byte) 0x14;

            } else if (auditC0SeIndex == cdLightSe) {
                environmentSid = (byte) 0x07;
            } else {
                throw new IllegalArgumentException("No recognizable PO detected.");
            }

            /*
             * Actual PO communication: operate through a single request the Calypso PO selection
             * and the file read
             */
            logger.info("The selection of the PO has succeeded.");

            MatchingSe selectedSe = selectionsResult.getActiveSelection().getMatchingSe();

            PoTransaction poTransaction =
                    new PoTransaction(poReader, (CalypsoPo) selectedSe, samReader, null);

            String name = "CNA Keyple Demo";

            boolean poProcessStatus =
                    poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                            PoTransaction.SessionAccessLevel.SESSION_LVL_PERSO, (byte) 0, (byte) 0);

            if (!poProcessStatus) {
                throw new IllegalStateException("processingOpening failure.");
            }


            poTransaction.prepareUpdateRecordCmd(environmentSid, (byte) 0x01, name.getBytes(),
                    "Environment");

            poProcessStatus = poTransaction.processClosing(TransmissionMode.CONTACTLESS,
                    ChannelState.KEEP_OPEN);

            if (!poProcessStatus) {
                throw new IllegalStateException("processClosing failure.");
            }

            logger.info(
                    "==================================================================================");
            logger.info(
                    "= End of the Calypso PO processing.                                              =");
            logger.info(
                    "==================================================================================");
        } else {
            logger.error("No PO were detected.");
        }
        System.exit(0);
    }
}
