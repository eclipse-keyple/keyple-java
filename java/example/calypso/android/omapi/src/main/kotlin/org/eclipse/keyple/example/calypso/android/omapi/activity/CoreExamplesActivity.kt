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
import org.eclipse.keyple.core.card.message.CardRequest
import org.eclipse.keyple.core.card.message.ChannelControl
import org.eclipse.keyple.core.card.message.ProxyReader
import org.eclipse.keyple.core.card.selection.CardSelection
import org.eclipse.keyple.core.card.selection.CardSelector
import org.eclipse.keyple.core.card.selection.CardSelector.AidSelector
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing
import org.eclipse.keyple.core.service.Reader
import org.eclipse.keyple.core.service.exception.KeypleReaderException
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.example.calypso.android.omapi.R
import org.eclipse.keyple.example.calypso.android.omapi.utils.AidEnum
import org.eclipse.keyple.example.calypso.android.omapi.utils.GenericCardSelectionRequest

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

            addActionEvent("Check First Reader Presency [reader.isCardPresent]")
            val isCardPresent = it.isCardPresent
            addResultEvent("ReaderIsPresent: [$isCardPresent]")
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
                    val cardRequest = CardRequest(cardSelector, null)

                    addActionEvent("Sending CardRequest to select: $poAid")
                    try {
                        val cardResponse = (it.value as ProxyReader).transmitCardRequest(cardRequest, ChannelControl.KEEP_OPEN)

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

        val cardSelection = CardSelection(MultiSelectionProcessing.PROCESS_ALL)

        /* Close the channel after the selection in order to secure the selection of all applications */
        cardSelection.prepareReleaseChannel()

        /* operate card selection (change the AID here to adapt it to the card used for the test) */
        val cardAidPrefix = "A000000404012509"

        /* AID based selection (1st selection, later indexed 0) */
        cardSelection.prepareSelection(GenericCardSelectionRequest(
                CardSelector.builder()
                        .aidSelector(AidSelector.builder()
                                .aidToSelect(cardAidPrefix)
                                .fileOccurrence(AidSelector.FileOccurrence.FIRST)
                                .fileControlInformation(AidSelector.FileControlInformation.FCI).build())
                        .build()))

        /* next selection (2nd selection, later indexed 1) */
        cardSelection.prepareSelection(GenericCardSelectionRequest(
                CardSelector.builder()
                .aidSelector(AidSelector.builder()
                        .aidToSelect(cardAidPrefix)
                        .fileOccurrence(AidSelector.FileOccurrence.NEXT)
                        .fileControlInformation(AidSelector.FileControlInformation.FCI).build())
                .build()))

        /* next selection (3rd selection, later indexed 2) */
        cardSelection.prepareSelection(GenericCardSelectionRequest(
                CardSelector.builder()
                        .aidSelector(AidSelector.builder()
                                .aidToSelect(cardAidPrefix)
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
                if (reader.isCardPresent) {
                    addActionEvent("Sending multiSelection request based on AID Prefix $cardAidPrefix to ${reader.name}")
                    try {
                        val selectionsResult = cardSelection.processExplicitSelection(reader)
                        if (selectionsResult.smartCards.size> 0) {
                            selectionsResult.smartCards.forEach {
                                val smartCard = it.value
                                addResultEvent("Selection status for selection " +
                                        "(indexed ${it.key}): \n\t\t" +
                                        "ATR: ${ByteArrayUtil.toHex(smartCard.atrBytes)}\n\t\t" +
                                        "FCI: ${ByteArrayUtil.toHex(smartCard.fciBytes)}")
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
        val cardAidPrefix = "A000000404012509"

        if (readers.size <1) {
            addResultEvent("No readers available")
        } else {
            readers.values.forEach { reader: Reader ->
                if (reader.isCardPresent) {

                    var cardSelection = CardSelection()

                    /*
                     * AID based selection: get the first application occurrence matching the AID, keep the
                     * physical channel open
                     */
                    cardSelection.prepareSelection(GenericCardSelectionRequest(
                            CardSelector.builder()
                                    .aidSelector(AidSelector.builder()
                                            .aidToSelect(cardAidPrefix)
                                            .fileOccurrence(AidSelector.FileOccurrence.FIRST)
                                            .fileControlInformation(AidSelector.FileControlInformation.FCI).build())
                                    .build()))
                    /* Do the selection and display the result */
                    doAndAnalyseSelection(reader, cardSelection, 1, cardAidPrefix)

                    /*
                     * New selection: get the next application occurrence matching the same AID, close the
                     * physical channel after
                     */
                    cardSelection = CardSelection()

                    /* Close the channel after the selection */
                    cardSelection.prepareReleaseChannel()

                    cardSelection.prepareSelection(GenericCardSelectionRequest(
                            CardSelector.builder()
                                    .aidSelector(AidSelector.builder()
                                            .aidToSelect(cardAidPrefix)
                                            .fileOccurrence(AidSelector.FileOccurrence.NEXT)
                                            .fileControlInformation(AidSelector.FileControlInformation.FCI).build())
                                    .build()))

                    /* Do the selection and display the result */
                    doAndAnalyseSelection(reader, cardSelection, 2, cardAidPrefix)
                } else {
                    addResultEvent("No cards were detected")
                }
            }
        }
        eventRecyclerView.smoothScrollToPosition(events.size - 1)
    }

    @Throws(KeypleReaderException::class)
    private fun doAndAnalyseSelection(reader: Reader, cardSelection: CardSelection, index: Int, cardAidPrefix: String) {
        addActionEvent("Sending multiSelection request based on AID Prefix $cardAidPrefix to ${reader.name}")
        val selectionsResult = cardSelection.processExplicitSelection(reader)
        if (selectionsResult.hasActiveSelection()) {
            val smartCard = selectionsResult.activeSmartCard
            addResultEvent("The card matched the selection $index.")

            addResultEvent("Selection status for case $index: \n\t\t" +
                    "ATR: ${ByteArrayUtil.toHex(smartCard.atrBytes)}\n\t\t" +
                    "FCI: ${ByteArrayUtil.toHex(smartCard.fciBytes)}")
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
