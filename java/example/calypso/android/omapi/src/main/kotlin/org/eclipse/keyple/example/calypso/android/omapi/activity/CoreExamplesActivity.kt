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
import kotlinx.android.synthetic.main.activity_core_examples.eventRecyclerView
import kotlinx.android.synthetic.main.activity_core_examples.toolbar
import org.eclipse.keyple.core.selection.SeSelection
import org.eclipse.keyple.core.seproxy.CardSelector
import org.eclipse.keyple.core.seproxy.CardSelector.AidSelector
import org.eclipse.keyple.core.seproxy.MultiSelectionProcessing
import org.eclipse.keyple.core.seproxy.Reader
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException
import org.eclipse.keyple.core.seproxy.message.CardRequest
import org.eclipse.keyple.core.seproxy.message.ChannelControl
import org.eclipse.keyple.core.seproxy.message.ProxyReader
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.example.calypso.android.omapi.R
import org.eclipse.keyple.example.calypso.android.omapi.utils.AidEnum
import org.eclipse.keyple.example.calypso.android.omapi.utils.GenericSeSelectionRequest

/**
 * Activity execution Keple-Core based examples.
 */
class CoreExamplesActivity : ExamplesActivity() {

    override fun initContentView() {
        setContentView(R.layout.activity_core_examples)
        initActionBar(toolbar, "keyple-core", "Shows usage of Keyple Core")
    }

    private fun getReadersInfos() {
        addHeaderEvent("Readers found ${readers.size}, getting infos")

        readers.values.forEach {

            addActionEvent("Get reader name [reader.name]")
            val name = it.name
            addResultEvent("Reader name: [$name]")

            addActionEvent("Check First Reader Presency [reader.isSePresent]")
            val isSePresent = it.isSePresent
            addResultEvent("ReaderIsPresent: [$isSePresent]")
        }

        eventRecyclerView.smoothScrollToPosition(events.size - 1)
    }

    private fun explicitSectionAid() {

        addHeaderEvent("UseCase Generic #1: AID based explicit selection")

        val aids = AidEnum.values().map { it.name }

        addChoiceEvent("Choose an Application:", aids) { selectedApp ->
            eventRecyclerView.smoothScrollToPosition(events.size - 1)
            val poAid = AidEnum.valueOf(selectedApp).aid

            if (readers.size <1) {
                addResultEvent("No readers available")
            } else {
                readers.forEach {
                    addHeaderEvent("Starting explicitAidSelection with $poAid on Reader ${it.key}")

                    val cardSelector = CardSelector.builder()
                            .aidSelector(AidSelector.builder().aidToSelect(poAid).build())
                            .build()
                    val seRequest = CardRequest(cardSelector, null)

                    addActionEvent("Sending CardRequest to select: $poAid")
                    try {
                        val cardResponse = (it.value as ProxyReader).transmitSeRequest(seRequest, ChannelControl.KEEP_OPEN)

                        if (cardResponse?.selectionStatus?.hasMatched() == true) {
                            addResultEvent("The selection of the PO has succeeded.")
                            addResultEvent("Application FCI = ${ByteArrayUtil.toHex(cardResponse.selectionStatus.fci.bytes)}")
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

    /**
     * Next may not be supported, depends on OMAPI implementation
     */
    private fun groupedMultiSelection() {
        addHeaderEvent("UseCase Generic #3: AID based grouped explicit multiple selection")

        val seSelection = SeSelection(MultiSelectionProcessing.PROCESS_ALL)

        /* Close the channel after the selection in order to secure the selection of all applications */
        seSelection.prepareReleaseSeChannel()

        /* operate card selection (change the AID here to adapt it to the card used for the test) */
        val seAidPrefix = "A000000404012509"

        /* AID based selection (1st selection, later indexed 0) */
        seSelection.prepareSelection(GenericSeSelectionRequest(
                CardSelector.builder()
                        .aidSelector(AidSelector.builder()
                                .aidToSelect(seAidPrefix)
                                .fileOccurrence(AidSelector.FileOccurrence.FIRST)
                                .fileControlInformation(AidSelector.FileControlInformation.FCI).build())
                        .build()))

        /* next selection (2nd selection, later indexed 1) */
        seSelection.prepareSelection(GenericSeSelectionRequest(
                CardSelector.builder()
                .aidSelector(AidSelector.builder()
                        .aidToSelect(seAidPrefix)
                        .fileOccurrence(AidSelector.FileOccurrence.NEXT)
                        .fileControlInformation(AidSelector.FileControlInformation.FCI).build())
                .build()))

        /* next selection (3rd selection, later indexed 2) */
        seSelection.prepareSelection(GenericSeSelectionRequest(
                CardSelector.builder()
                        .aidSelector(AidSelector.builder()
                                .aidToSelect(seAidPrefix)
                                .fileOccurrence(AidSelector.FileOccurrence.NEXT)
                                .fileControlInformation(AidSelector.FileControlInformation.FCI).build())
                        .build()))

        /*
         * Actual card communication: operate through a single request the card selection
         */
        if (readers.size <1) {
            addResultEvent("No readers available")
        } else {
            readers.values.forEach { reader: Reader ->
                if (reader.isSePresent) {
                    addActionEvent("Sending multiSelection request based on AID Prefix $seAidPrefix to ${reader.name}")
                    try {
                        val selectionsResult = seSelection.processExplicitSelection(reader)
                        if (selectionsResult.matchingSelections.size> 0) {
                            selectionsResult.matchingSelections.forEach {
                                val matchingSe = it.value
                                addResultEvent("Selection status for selection " +
                                        "(indexed ${it.key}): \n\t\t" +
                                        "ATR: ${ByteArrayUtil.toHex(matchingSe.atrBytes)}\n\t\t" +
                                        "FCI: ${ByteArrayUtil.toHex(matchingSe.fciBytes)}")
                            }
                        } else {
                            addResultEvent("No cards matched the selection.")
                        }
                    } catch (e: Exception) {
                        addResultEvent("The selection of the PO Failed: ${e.message}")
                    }
                } else {
                    addResultEvent("No cards were detected")
                }
            }
        }
        eventRecyclerView.smoothScrollToPosition(events.size - 1)
    }

    /**
     * Next may not be supported, depends on OMAPI implementation
     */
    private fun sequentialMultiSelection() {
        addHeaderEvent("UseCase Generic #4: AID based sequential explicit multiple selection")

        /* operate card selection (change the AID here to adapt it to the card used for the test) */
        val seAidPrefix = "A000000404012509"

        if (readers.size <1) {
            addResultEvent("No readers available")
        } else {
            readers.values.forEach { reader: Reader ->
                if (reader.isSePresent) {

                    var seSelection = SeSelection()

                    /*
                     * AID based selection: get the first application occurrence matching the AID, keep the
                     * physical channel open
                     */
                    seSelection.prepareSelection(GenericSeSelectionRequest(
                            CardSelector.builder()
                                    .aidSelector(AidSelector.builder()
                                            .aidToSelect(seAidPrefix)
                                            .fileOccurrence(AidSelector.FileOccurrence.FIRST)
                                            .fileControlInformation(AidSelector.FileControlInformation.FCI).build())
                                    .build()))
                    /* Do the selection and display the result */
                    doAndAnalyseSelection(reader, seSelection, 1, seAidPrefix)

                    /*
                     * New selection: get the next application occurrence matching the same AID, close the
                     * physical channel after
                     */
                    seSelection = SeSelection()

                    /* Close the channel after the selection */
                    seSelection.prepareReleaseSeChannel()

                    seSelection.prepareSelection(GenericSeSelectionRequest(
                            CardSelector.builder()
                                    .aidSelector(AidSelector.builder()
                                            .aidToSelect(seAidPrefix)
                                            .fileOccurrence(AidSelector.FileOccurrence.NEXT)
                                            .fileControlInformation(AidSelector.FileControlInformation.FCI).build())
                                    .build()))

                    /* Do the selection and display the result */
                    doAndAnalyseSelection(reader, seSelection, 2, seAidPrefix)
                } else {
                    addResultEvent("No cards were detected")
                }
            }
        }
        eventRecyclerView.smoothScrollToPosition(events.size - 1)
    }

    @Throws(KeypleReaderException::class)
    private fun doAndAnalyseSelection(reader: Reader, seSelection: SeSelection, index: Int, seAidPrefix: String) {
        addActionEvent("Sending multiSelection request based on AID Prefix $seAidPrefix to ${reader.name}")
        val selectionsResult = seSelection.processExplicitSelection(reader)
        if (selectionsResult.hasActiveSelection()) {
            val matchingSe = selectionsResult.activeMatchingSe
            addResultEvent("The card matched the selection $index.")

            addResultEvent("Selection status for case $index: \n\t\t" +
                    "ATR: ${ByteArrayUtil.toHex(matchingSe.atrBytes)}\n\t\t" +
                    "FCI: ${ByteArrayUtil.toHex(matchingSe.fciBytes)}")
        } else {
            addResultEvent("The selection did not match for case $index.")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        super.onNavigationItemSelected(item)
        when (item.itemId) {
            R.id.getReadersInfosButton -> getReadersInfos()
            R.id.explicitSelectionAidButton -> explicitSectionAid()
            R.id.groupedMultiselectionButton -> groupedMultiSelection()
            R.id.sequentialMultiSelectionButton -> sequentialMultiSelection()
        }
        return true
    }
}
