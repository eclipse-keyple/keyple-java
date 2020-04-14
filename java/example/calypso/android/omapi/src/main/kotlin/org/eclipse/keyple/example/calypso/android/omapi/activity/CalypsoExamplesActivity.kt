/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.calypso.android.omapi.activity

import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_calypso_example.eventRecyclerView
import kotlinx.android.synthetic.main.activity_calypso_example.toolbar
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest
import org.eclipse.keyple.calypso.transaction.PoSelector
import org.eclipse.keyple.core.selection.SeSelection
import org.eclipse.keyple.core.seproxy.ChannelControl
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.example.calypso.android.omapi.R
import org.eclipse.keyple.example.calypso.android.omapi.utils.AidEnum

class CalypsoExamplesActivity : ExamplesActivity() {

    override fun initContentView() {
        setContentView(R.layout.activity_calypso_example)
        initActionBar(toolbar, "keyple-calypso", "Shows usage of Keyple Calypso")
    }

    private fun explicitSectionAid() {

        val aids = AidEnum.values().map { it.name }

        addChoiceEvent("Choose an Application:", aids) { selectedApp ->
            eventRecyclerView.smoothScrollToPosition(events.size - 1)
            val poAid = AidEnum.valueOf(selectedApp).aid

            if (readers.size < 1) {
                addResultEvent("No readers available")
            } else {
                readers.forEach { seReader: SeReader ->
                    addHeaderEvent("Starting explicitAidSelection with $poAid on Reader ${seReader.name}")
                    /*
                     * Prepare a Calypso PO selection. Default parameters:
                     * Select the first application matching the selection AID whatever the SE
                     * communication protocol and keep the logical channel open after the selection
                     */
                    val seSelection = SeSelection()

                    /*
                     * Configuration of Selection request
                     * Setting of an AID based selection of a Calypso REV3 PO
                     *
                     */
                    val poSelectionRequest = PoSelectionRequest(
                            PoSelector(SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                                    PoSelector.PoAidSelector(
                                            SeSelector.AidSelector.IsoAid(poAid),
                                            PoSelector.InvalidatedPo.REJECT),
                                    "AID: $poAid"))
                    seSelection.prepareSelection(poSelectionRequest)

                    try {
                        addActionEvent("Process explicit selection")
                        val selectionsResult = seSelection.processExplicitSelection(seReader)

                        /**
                         * Check if PO has been selected successfuly
                         */
                        if (selectionsResult.hasActiveSelection()) {
                            val matchedSe = selectionsResult.activeSelection.matchingSe
                            addResultEvent("The selection of the SE has succeeded.")
                            addResultEvent("Application FCI = ${ByteArrayUtil.toHex(matchedSe.selectionStatus.fci.bytes)}")
                        } else {
                            addResultEvent("The selection of the PO Failed")
                        }
                    } catch (e: Exception) {
                        addResultEvent("The selection of the PO Failed: ${e.message}")
                    }
                }
            }
            eventRecyclerView.smoothScrollToPosition(events.size - 1)
        }
    }

    private fun readEnvironmentAndUsage() {
        val aids = AidEnum.values().filter { it == AidEnum.HOPLINK || it == AidEnum.NAVIGO2013 }.map { it.name }

        addChoiceEvent("Choose an Application:", aids) { selectedApp ->
            eventRecyclerView.smoothScrollToPosition(events.size - 1)
            readEnvironmentAndUsage(AidEnum.valueOf(selectedApp))
            eventRecyclerView.smoothScrollToPosition(events.size - 1)
        }
    }

    private fun readEnvironmentAndUsage(aidEnum: AidEnum) {

        if (readers.size < 1) {
            addResultEvent("No readers available")
        } else {
            readers.forEach { seReader: SeReader ->
                if (aidEnum == AidEnum.NAVIGO2013) {

                    val poAid = aidEnum.aid
                    val sfiNavigoEFEnvironment = 0x07.toByte()
                    val sfiNavigoEFTransportEvent = 0x08.toByte()

                    addHeaderEvent("Starting readEnvironmentAndUsage with: $poAid on Reader ${seReader.name}")

                    /*
                     * Prepare a Calypso PO selection
                     */
                    val seSelection = SeSelection()
                    val poSelectionRequest = PoSelectionRequest(
                            PoSelector(SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                                    PoSelector.PoAidSelector(
                                            SeSelector.AidSelector.IsoAid(poAid),
                                            PoSelector.InvalidatedPo.REJECT),
                                    "AID: $poAid"))

                    /*
                     * Prepare the reading order and keep the associated parser for later use once
                     * the selection has been made.
                     */
                    val readEnvironmentParserIndex = poSelectionRequest.prepareReadRecords(
                            sfiNavigoEFEnvironment, ReadDataStructure.SINGLE_RECORD_DATA,
                            1.toByte(), 29)

                    val readTransportEventParserIndex = poSelectionRequest.prepareReadRecords(
                            sfiNavigoEFTransportEvent, ReadDataStructure.SINGLE_RECORD_DATA,
                            1.toByte(), 29)

                    /*
                     * Add the selection case to the current selection (we could have added other
                     * cases here)
                     *
                     * Ignore the returned index since we have only one selection here.
                     */
                    seSelection.prepareSelection(poSelectionRequest)

                    /*
                     * Actual PO communication: operate through a single request the Calypso PO
                     * selection and the file read
                     */
                    addActionEvent("Process explicit selection for $poAid and reading Environment and transport event")
                    try {
                        val selectionsResult = seSelection.processExplicitSelection(seReader)

                        if (selectionsResult.hasActiveSelection()) {
                            val matchingSelection = selectionsResult.activeSelection

                            // val calypsoPo = matchingSelection.matchingSe as CalypsoPo
                            addResultEvent("Selection succeeded for P0 with aid $poAid")

                            val readEnvironmentParser = matchingSelection
                                    .getResponseParser(readEnvironmentParserIndex) as ReadRecordsRespPars

                            /*
                             * Retrieve the data read from the parser updated during the selection
                             * process (Environment)
                             */
                            val environmentAndHolder = readEnvironmentParser.records[1]
                            addResultEvent("Environment file data: ${ByteArrayUtil.toHex(environmentAndHolder)}")

                            val readTransportEventParser = matchingSelection
                                    .getResponseParser(readTransportEventParserIndex) as ReadRecordsRespPars

                            /*
                             * Retrieve the data read from the parser updated during the selection
                             * process (Usage)
                             */
                            val transportEvents = readTransportEventParser.records[1]
                            addResultEvent("Transport Event file data: ${ByteArrayUtil.toHex(transportEvents)}")
                        } else {
                            addResultEvent("The selection of the PO Failed")
                        }
                    } catch (e: Exception) {
                        addResultEvent("The selection of the PO Failed: ${e.message}")
                    }
                } else if (aidEnum == AidEnum.HOPLINK) {
                    val poAid = aidEnum.aid
                    val sfiHoplinkEFEnvironment = 0x14.toByte()
                    val sfiHoplinkEFUsage = 0x1A.toByte()

                    addHeaderEvent("Starting readEnvironmentAndUsage with: $poAid on Reader ${seReader.name}")

                    val t2UsageRecord1_dataFill = ("0102030405060708090A0B0C0D0E0F10" + "1112131415161718191A1B1C1D1E1F20" +
                            "2122232425262728292A2B2C2D2E2F30")

                    /*
                     * Prepare a Calypso PO selection
                     */
                    val seSelection = SeSelection(MultiSeRequestProcessing.FIRST_MATCH,
                            ChannelControl.KEEP_OPEN)

                    /*
                     * Setting of an AID based selection of a Calypso REV3 PO
                     *
                     * Select the first application matching the selection AID whatever the SE
                     * communication protocol keep the logical channel open after the selection
                     */

                    /*
                     * Calypso selection: configures a PoSelectionRequest with all the desired
                     * attributes to make the selection and read additional information afterwards
                     */
                    val poSelectionRequest = PoSelectionRequest(
                            PoSelector(SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                                    PoSelector.PoAidSelector(
                                            SeSelector.AidSelector.IsoAid(poAid),
                                            PoSelector.InvalidatedPo.REJECT),
                                    "AID: $poAid"))

                    /*
                     * Prepare the reading order and keep the associated parser for later use once
                     * the selection has been made.
                     */
                    val readEnvironmentParserIndex = poSelectionRequest.prepareReadRecords(
                            sfiHoplinkEFEnvironment, ReadDataStructure.SINGLE_RECORD_DATA,
                            1.toByte(), 32)

                    val readUsageParserIndex = poSelectionRequest.prepareReadRecords(
                            sfiHoplinkEFUsage, ReadDataStructure.SINGLE_RECORD_DATA,
                            1.toByte(), 48)

                    /*
                     * Add the selection case to the current selection (we could have added other
                     * cases here)
                     *
                     * Ignore the returned index since we have only one selection here.
                     */
                    seSelection.prepareSelection(poSelectionRequest)

                    /*
                     * Actual PO communication: operate through a single request the Calypso PO
                     * selection and the file read
                     */
                    try {
                        val selectionsResult = seSelection.processExplicitSelection(seReader)

                        if (selectionsResult.hasActiveSelection()) {
                            val matchingSelection = selectionsResult.activeSelection

                            // val calypsoPo = matchingSelection.matchingSe as CalypsoPo
                            addResultEvent("Selection succeeded for P0 with aid $poAid")

                            val readEnvironmentParser = matchingSelection
                                    .getResponseParser(readEnvironmentParserIndex) as ReadRecordsRespPars

                            /*
                             * Retrieve the data read from the parser updated during the selection
                             * process (Environment)
                             */
                            val environmentAndHolder = readEnvironmentParser.records[1]
                            addResultEvent("Environment file data: ${ByteArrayUtil.toHex(environmentAndHolder)}")

                            val readUsageParser = matchingSelection
                                    .getResponseParser(readUsageParserIndex) as ReadRecordsRespPars

                            /*
                             * Retrieve the data read from the parser updated during the selection
                             * process (Usage)
                             */
                            val transportEvents = readUsageParser.records[1]
                            addResultEvent("Transport Event file data: ${ByteArrayUtil.toHex(transportEvents)}")
                        } else {
                            addResultEvent("The selection of the PO Failed")
                        }
                    } catch (e: Exception) {
                        addResultEvent("The selection of the PO Failed: ${e.message}")
                    }
                }
            }
        }
        eventRecyclerView.smoothScrollToPosition(events.size - 1)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        super.onNavigationItemSelected(item)
        when (item.itemId) {
            R.id.explicitSelectionAidButton -> explicitSectionAid()
            R.id.readEnvironnementAndUsageButton -> readEnvironmentAndUsage()
        }
        return true
    }
}
