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
package org.eclipse.keyple.example.activity

import android.view.MenuItem
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_core_examples.drawerLayout
import kotlinx.android.synthetic.main.activity_core_examples.eventRecyclerView
import kotlinx.android.synthetic.main.activity_core_examples.toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.keyple.core.command.AbstractApduCommandBuilder
import org.eclipse.keyple.core.selection.AbstractMatchingSe
import org.eclipse.keyple.core.selection.AbstractSeSelectionRequest
import org.eclipse.keyple.core.selection.SeSelection
import org.eclipse.keyple.core.seproxy.MultiSelectionProcessing
import org.eclipse.keyple.core.seproxy.Reader
import org.eclipse.keyple.core.seproxy.CardSelector
import org.eclipse.keyple.core.seproxy.CardSelector.AidSelector
import org.eclipse.keyple.core.seproxy.event.ObservableReader
import org.eclipse.keyple.core.seproxy.event.ReaderEvent
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException
import org.eclipse.keyple.core.seproxy.message.CardResponse
import org.eclipse.keyple.core.seproxy.plugin.reader.util.ContactlessCardCommonProtocols
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.example.calypso.android.nfc.R
import org.eclipse.keyple.example.util.CalypsoClassicInfo
import timber.log.Timber

class CoreExamplesActivity : AbstractExampleActivity() {

    override fun onResume() {
        super.onResume()
        reader.enableNFCReaderMode(this)
    }

    override fun initContentView() {
        setContentView(R.layout.activity_core_examples)
        initActionBar(toolbar, "NFC Plugins", "Core Examples")
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        when (item.itemId) {
            R.id.usecase1 -> {
                clearEvents()
                configureUseCase1ExplicitSelectionAid()
            }
            R.id.usecase2 -> {
                clearEvents()
                configureUseCase2DefaultSelectionNotification()
            }
            R.id.usecase3 -> {
                clearEvents()
                configureUseCase3GroupedMultiSelection()
            }
            R.id.usecase4 -> {
                clearEvents()
                configureUseCase4SequentialMultiSelection()
            }
        }
        return true
    }

    private fun configureUseCase4SequentialMultiSelection() {
        addHeaderEvent("UseCase Generic #4: AID based sequential explicit multiple selection")
        addHeaderEvent("Reader  NAME = ${reader.name}")

        /*Check if a card is present in the reader */
        if (reader.isSePresent) {
            /*
              * operate card AID selection (change the AID prefix here to adapt it to the card used for
              * the test [the card should have at least two applications matching the AID prefix])
              */
            val seAidPrefix = CalypsoClassicInfo.AID_PREFIX

            /* First selection case */
            seSelection = SeSelection()

            /* AID based selection (1st selection, later indexed 0) */
            seSelection.prepareSelection(
                    GenericSeSelectionRequest(
                            CardSelector.builder()
                                    .seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name)
                                    .aidSelector(AidSelector.builder()
                                            .aidToSelect(seAidPrefix)
                                            .fileOccurrence(CardSelector.AidSelector.FileOccurrence.FIRST)
                                            .fileControlInformation(CardSelector.AidSelector.FileControlInformation.FCI).build())
                                    .build()))

            /* Do the selection and display the result */
            addActionEvent("FIRST MATCH Calypso PO selection for prefix: $seAidPrefix")
            doAndAnalyseSelection(reader, seSelection, 1)

            /*
              * New selection: get the next application occurrence matching the same AID, close the
              * physical channel after
              */
            seSelection = SeSelection()

            /* Close the channel after the selection */
            seSelection.prepareReleaseSeChannel()

            /* next selection (2nd selection, later indexed 1) */
            seSelection.prepareSelection(
                    GenericSeSelectionRequest(
                            CardSelector.builder()
                                    .seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name)
                                    .aidSelector(CardSelector.AidSelector.builder()
                                            .aidToSelect(seAidPrefix)
                                            .fileOccurrence(CardSelector.AidSelector.FileOccurrence.NEXT)
                                            .fileControlInformation(CardSelector.AidSelector.FileControlInformation.FCI).build())
                                    .build()))

            /* Do the selection and display the result */
            addActionEvent("NEXT MATCH Calypso PO selection for prefix: $seAidPrefix")
            doAndAnalyseSelection(reader, seSelection, 2)
        } else {
            addResultEvent("No cards were detected.")
        }
        eventRecyclerView.smoothScrollToPosition(events.size - 1)
    }

    private fun doAndAnalyseSelection(reader: Reader, seSelection: SeSelection, index: Int) {
        try {
            val selectionsResult = seSelection.processExplicitSelection(reader)
            if (selectionsResult.hasActiveSelection()) {
                    val matchingSe = selectionsResult.activeMatchingSe
                    addResultEvent("Selection status for selection " +
                            "(indexed $index): \n\t\t" +
                            "ATR: ${ByteArrayUtil.toHex(matchingSe.atrBytes)}\n\t\t" +
                            "FCI: ${ByteArrayUtil.toHex(matchingSe.fciBytes)}")
            } else {
                addResultEvent("The selection did not match for case $index.")
            }
        } catch (e: KeypleReaderException) {
            addResultEvent("Error: ${e.message}")
        }
    }

    private fun configureUseCase3GroupedMultiSelection() {
        addHeaderEvent("UseCase Generic #3: AID based grouped explicit multiple selection")
        addHeaderEvent("Reader  NAME = ${reader.name}")

        useCase = null

        if (reader.isSePresent) {
            seSelection = SeSelection(MultiSelectionProcessing.PROCESS_ALL)

            /* Close the channel after the selection to force the selection of all applications */
            seSelection.prepareReleaseSeChannel()

            /* operate card selection (change the AID here to adapt it to the card used for the test) */
            val seAidPrefix = CalypsoClassicInfo.AID_PREFIX

            /* AID based selection (1st selection, later indexed 0) */
            seSelection.prepareSelection(
                    GenericSeSelectionRequest(
                            CardSelector.builder()
                                    .seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name)
                                    .aidSelector(CardSelector.AidSelector.builder()
                                            .aidToSelect(seAidPrefix)
                                            .fileOccurrence(CardSelector.AidSelector.FileOccurrence.FIRST)
                                            .fileControlInformation(CardSelector.AidSelector.FileControlInformation.FCI).build())
                                    .build()))

            /* next selection (2nd selection, later indexed 1) */
            seSelection.prepareSelection(
                    GenericSeSelectionRequest(
                            CardSelector.builder()
                                    .seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name)
                                    .aidSelector(CardSelector.AidSelector.builder()
                                            .aidToSelect(seAidPrefix)
                                            .fileOccurrence(CardSelector.AidSelector.FileOccurrence.NEXT)
                                            .fileControlInformation(CardSelector.AidSelector.FileControlInformation.FCI).build())
                                    .build()))

            /* next selection (3rd selection, later indexed 2) */
            seSelection.prepareSelection(
                    GenericSeSelectionRequest(
                            CardSelector.builder()
                                    .seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name)
                                    .aidSelector(CardSelector.AidSelector.builder()
                                            .aidToSelect(seAidPrefix)
                                            .fileOccurrence(CardSelector.AidSelector.FileOccurrence.NEXT)
                                            .fileControlInformation(CardSelector.AidSelector.FileControlInformation.FCI).build())
                                    .build()))

            addActionEvent("Calypso PO selection for prefix: $seAidPrefix")

            /*
            * Actual card communication: operate through a single request the card selection
            */
            try {
                val selectionResult = seSelection.processExplicitSelection(reader)

                if (selectionResult.matchingSelections.size > 0) {
                    selectionResult.matchingSelections.forEach {
                        val matchingSe = it.value
                        addResultEvent("Selection status for selection " +
                                "(indexed ${it.key}): \n\t\t" +
                                "ATR: ${ByteArrayUtil.toHex(matchingSe.atrBytes)}\n\t\t" +
                                "FCI: ${ByteArrayUtil.toHex(matchingSe.fciBytes)}")
                    }
                    addResultEvent("End of selection")
                } else {
                    addResultEvent("No cards matched the selection.")
                    addResultEvent("The card must be in the field when starting this use case")
                }
            } catch (e: KeypleReaderException) {
                addResultEvent("Error: ${e.message}")
            }
        } else {
            addResultEvent("No cards were detected.")
        }

        eventRecyclerView.smoothScrollToPosition(events.size - 1)
    }

    private fun configureUseCase2DefaultSelectionNotification() {
        addHeaderEvent("UseCase Generic #2: AID based default selection")
        addHeaderEvent("Reader  NAME = ${reader.name}")

        /*
         * Prepare a card selection
         */
        seSelection = SeSelection()

        /*
        * Setting of an AID based selection
        *
        * Select the first application matching the selection AID whatever the card communication
        * protocol keep the logical channel open after the selection
        */
        val aid = CalypsoClassicInfo.AID

        /*
         * Generic selection: configures a CardSelector with all the desired attributes to make the
         * selection
         */
        val cardSelector = GenericSeSelectionRequest(CardSelector.builder()
                .seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name)
                .aidSelector(AidSelector.builder()
                        .aidToSelect(aid).build())
                .build())

        /*
        * Add the selection case to the current selection (we could have added other cases here)
        */
        seSelection.prepareSelection(cardSelector)

        /*
         * Provide the Reader with the selection operation to be processed when a card is inserted.
         */
        (reader as ObservableReader).setDefaultSelectionRequest(seSelection.selectionOperation,
                ObservableReader.NotificationMode.MATCHED_ONLY, ObservableReader.PollingMode.REPEATING)

        // (reader as ObservableReader).addObserver(this) //ALready done in onCreate

        addActionEvent("Waiting for a card... The default AID based selection to be processed as soon as the card is detected.")

        useCase = object : UseCase {
            override fun onEventUpdate(event: ReaderEvent?) {
                CoroutineScope(Dispatchers.Main).launch {
                    when (event?.eventType) {
                        ReaderEvent.EventType.SE_MATCHED -> {
                            addResultEvent("SE_MATCHED event: A card corresponding to request has been detected")
                            val selectedSe = seSelection.processDefaultSelection(event.defaultSelectionsResponse).activeMatchingSe
                            if (selectedSe != null) {
                                addResultEvent("Observer notification: the selection of the card has succeeded. End of the card processing.")
                                addResultEvent("Application FCI = ${ByteArrayUtil.toHex(selectedSe.fciBytes)}")
                            } else {
                                addResultEvent("The selection of the card has failed. Should not have occurred due to the MATCHED_ONLY selection mode.")
                            }
                        }

                        ReaderEvent.EventType.SE_INSERTED -> {
                            addResultEvent("SE_INSERTED event: should not have occurred due to the MATCHED_ONLY selection mode.")
                        }

                        ReaderEvent.EventType.SE_REMOVED -> {
                            addResultEvent("SE_REMOVED event: There is no PO inserted anymore. Return to the waiting state...")
                        }

                        else -> { }
                    }
                    eventRecyclerView.smoothScrollToPosition(events.size - 1)
                }
                if (event?.eventType == ReaderEvent.EventType.SE_INSERTED || event?.eventType == ReaderEvent.EventType.SE_MATCHED) {
                    // TODO make this conditional on the abnormal termination (exception)
                    /*
                     * Informs the underlying layer of the end of the card processing, in order to manage the
                     * removal sequence. <p>If closing has already been requested, this method will do
                     * nothing.
                     */
                    try {
                        (event.reader as ObservableReader).finalizeSeProcessing()
                    } catch (e: KeypleReaderNotFoundException) {
                        Timber.e(e)
                        addResultEvent("Error: ${e.message}")
                    } catch (e: KeyplePluginNotFoundException) {
                        Timber.e(e)
                        addResultEvent("Error: ${e.message}")
                    }
                }
                eventRecyclerView.smoothScrollToPosition(events.size - 1)
            }
        }
    }

    private fun configureUseCase1ExplicitSelectionAid() {
        addHeaderEvent("UseCase Generic #1: Explicit AID selection")
        addHeaderEvent("Reader  NAME = ${reader.name}")

        if (reader.isSePresent) {

            /*
             * Prepare the card selection
             */
            seSelection = SeSelection()

            /*
             * Setting of an AID based selection (in this example a Calypso REV3 PO)
             *
             * Select the first application matching the selection AID whatever the card communication
             * protocol keep the logical channel open after the selection
             */
            val aid = CalypsoClassicInfo.AID

            /*
             * Generic selection: configures a CardSelector with all the desired attributes to make
             * the selection and read additional information afterwards
             */
            val genericSeSelectionRequest = GenericSeSelectionRequest(
                    CardSelector.builder().seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name).aidSelector(
                            AidSelector.builder().aidToSelect(aid).build()).build())

            /**
             * Prepare Selection
             */
            seSelection.prepareSelection(genericSeSelectionRequest)

            /*
             * Provide the Reader with the selection operation to be processed when a card is inserted.
             */
            (reader as ObservableReader).setDefaultSelectionRequest(seSelection.selectionOperation,
                    ObservableReader.NotificationMode.MATCHED_ONLY, ObservableReader.PollingMode.SINGLESHOT)

            /**
             * We won't be listening for event update within this use case
             */
            useCase = null

            addActionEvent("Calypso PO selection: $aid")
            try {
                val selectionsResult = seSelection.processExplicitSelection(reader)

                if (selectionsResult.hasActiveSelection()) {
                    val matchedSe = selectionsResult.activeMatchingSe
                    addResultEvent("The selection of the card has succeeded.")
                    addResultEvent("Application FCI = ${ByteArrayUtil.toHex(matchedSe.fciBytes)}")
                    addResultEvent("End of the generic card processing.")
                } else {
                    addResultEvent("The selection of the card has failed.")
                }
            } catch (e: KeypleReaderException) {
                Timber.e(e)
                addResultEvent("Error: ${e.message}")
            }
        } else {
            addResultEvent("No cards were detected.")
            addResultEvent("The card must be in the field when starting this use case")
        }
        eventRecyclerView.smoothScrollToPosition(events.size - 1)
    }

    override fun update(event: ReaderEvent?) {
        Timber.i("New ReaderEvent received : $event")
        useCase?.onEventUpdate(event)
    }

    /**
     * Create a new class extending AbstractSeSelectionRequest
     */
    inner class GenericSeSelectionRequest(cardSelector: CardSelector) : AbstractSeSelectionRequest<AbstractApduCommandBuilder>(cardSelector) {
        override fun parse(cardResponse: CardResponse): AbstractMatchingSe {
            class GenericMatchingSe(
                selectionResponse: CardResponse
            ) : AbstractMatchingSe(selectionResponse)
            return GenericMatchingSe(cardResponse)
        }
    }
}
