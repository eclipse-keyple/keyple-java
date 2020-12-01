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
import org.eclipse.keyple.calypso.transaction.CalypsoPo
import org.eclipse.keyple.calypso.transaction.PoSelection
import org.eclipse.keyple.calypso.transaction.PoSelector
import org.eclipse.keyple.calypso.transaction.PoSelector.InvalidatedPo
import org.eclipse.keyple.core.card.selection.CardSelectionsService
import org.eclipse.keyple.core.card.selection.CardSelector.AidSelector
import org.eclipse.keyple.core.service.Reader
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
                readers.values.forEach { reader: Reader ->
                    addHeaderEvent("Starting explicitAidSelection with $poAid on Reader ${reader.name}")
                    /*
                     * Prepare a Calypso PO selection. Default parameters:
                     * Select the first application matching the selection AID whatever the card
                     * communication protocol and keep the logical channel open after the selection
                     */
                    val cardSelectionsService = CardSelectionsService()

                    /*
                     * Configuration of Selection request
                     * Setting of an AID based selection of a Calypso REV3 PO
                     *
                     */
                    val poSelection = PoSelection(
                            PoSelector.builder()
                                    .aidSelector(AidSelector.builder().aidToSelect(poAid).build())
                                        .invalidatedPo(InvalidatedPo.REJECT).build())
                    cardSelectionsService.prepareSelection(poSelection)

                    try {
                        addActionEvent("Process explicit selection")
                        val cardSelectionsResult = cardSelectionsService.processExplicitSelections(reader)

                        /**
                         * Check if PO has been selected successfuly
                         */
                        if (cardSelectionsResult.hasActiveSelection()) {
                            val matchedCard = cardSelectionsResult.activeSmartCard
                            addResultEvent("The selection of the card has succeeded.")
                            addResultEvent("Application FCI = ${ByteArrayUtil.toHex(matchedCard.fciBytes)}")
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
            readers.values.forEach { reader: Reader ->
                if (aidEnum == AidEnum.NAVIGO2013) {

                    val poAid = aidEnum.aid
                    val sfiNavigoEFEnvironment = 0x07.toByte()
                    val sfiNavigoEFTransportEvent = 0x08.toByte()

                    addHeaderEvent("Starting readEnvironmentAndUsage with: $poAid on Reader ${reader.name}")

                    /*
                     * Prepare a Calypso PO selection
                     */
                    val cardSelectionsService = CardSelectionsService()
                    val poSelection = PoSelection(
                            PoSelector.builder()
                                    .aidSelector(AidSelector.builder().aidToSelect(poAid).build())
                                    .invalidatedPo(InvalidatedPo.REJECT).build())

                    /*
                     * Prepare the reading order and keep the associated parser for later use once
                     * the selection has been made.
                     */
                    poSelection.prepareReadRecordFile(
                            sfiNavigoEFEnvironment, 1)

                    poSelection.prepareReadRecordFile(
                            sfiNavigoEFTransportEvent, 1)

                    /*
                     * Add the selection case to the current selection (we could have added other
                     * cases here)
                     *
                     * Ignore the returned index since we have only one selection here.
                     */
                    cardSelectionsService.prepareSelection(poSelection)

                    /*
                     * Actual PO communication: operate through a single request the Calypso PO
                     * selection and the file read
                     */
                    addActionEvent("Process explicit selection for $poAid and reading Environment and transport event")
                    try {
                        val cardSelectionsResult = cardSelectionsService.processExplicitSelections(reader)

                        if (cardSelectionsResult.hasActiveSelection()) {
                            val calypsoPo = cardSelectionsResult.activeSmartCard as CalypsoPo

                            addResultEvent("Selection succeeded for P0 with aid $poAid")

                            val environmentAndHolder = calypsoPo.getFileBySfi(sfiNavigoEFEnvironment).data.content
                            addResultEvent("Environment file data: ${ByteArrayUtil.toHex(environmentAndHolder)}")

                            val transportEvent = calypsoPo.getFileBySfi(sfiNavigoEFTransportEvent).data.content
                            addResultEvent("Transport Event file data: ${ByteArrayUtil.toHex(transportEvent)}")
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

                    addHeaderEvent("Starting readEnvironmentAndUsage with: $poAid on Reader ${reader.name}")

                    val t2UsageRecord1_dataFill = ("0102030405060708090A0B0C0D0E0F10" + "1112131415161718191A1B1C1D1E1F20" +
                            "2122232425262728292A2B2C2D2E2F30")

                    /*
                     * Prepare a Calypso PO selection
                     */
                    val cardSelectionsService = CardSelectionsService()

                    /*
                     * Setting of an AID based selection of a Calypso REV3 PO
                     *
                     * Select the first application matching the selection AID whatever the card
                     * communication protocol keep the logical channel open after the selection
                     */

                    /*
                     * Calypso selection: configures a PoSelection with all the desired
                     * attributes to make the selection and read additional information afterwards
                     */
                    val poSelection = PoSelection(
                            PoSelector.builder()
                                    .aidSelector(AidSelector.builder().aidToSelect(poAid).build())
                                    .invalidatedPo(InvalidatedPo.REJECT).build())

                    /*
                     * Prepare the reading order and keep the associated parser for later use once
                     * the selection has been made.
                     */
                    poSelection.prepareReadRecordFile(sfiHoplinkEFEnvironment, 1)

                    poSelection.prepareReadRecordFile(
                            sfiHoplinkEFUsage, 1)

                    /*
                     * Add the selection case to the current selection (we could have added other
                     * cases here)
                     *
                     * Ignore the returned index since we have only one selection here.
                     */
                    cardSelectionsService.prepareSelection(poSelection)

                    /*
                     * Actual PO communication: operate through a single request the Calypso PO
                     * selection and the file read
                     */
                    try {
                        val cardSelectionsResult = cardSelectionsService.processExplicitSelections(reader)

                        if (cardSelectionsResult.hasActiveSelection()) {
                            val calypsoPo = cardSelectionsResult.activeSmartCard as CalypsoPo

                            // val calypsoPo = matchingSelection.smartCard as CalypsoPo
                            addResultEvent("Selection succeeded for P0 with aid $poAid")

                            val environmentAndHolder = calypsoPo.getFileBySfi(sfiHoplinkEFEnvironment).data.content
                            addResultEvent("Environment file data: ${ByteArrayUtil.toHex(environmentAndHolder)}")

                            val usage = calypsoPo.getFileBySfi(sfiHoplinkEFUsage).data.content
                            addResultEvent("Environment file data: ${ByteArrayUtil.toHex(usage)}")
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
