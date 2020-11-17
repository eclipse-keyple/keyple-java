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
import org.eclipse.keyple.core.card.command.AbstractApduCommandBuilder
import org.eclipse.keyple.core.card.message.CardSelectionResponse
import org.eclipse.keyple.core.card.selection.AbstractCardSelectionRequest
import org.eclipse.keyple.core.card.selection.AbstractSmartCard
import org.eclipse.keyple.core.card.selection.CardSelection
import org.eclipse.keyple.core.card.selection.CardSelector
import org.eclipse.keyple.core.card.selection.CardSelector.AidSelector
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing
import org.eclipse.keyple.core.service.Reader
import org.eclipse.keyple.core.service.event.ObservableReader
import org.eclipse.keyple.core.service.event.ReaderEvent
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException
import org.eclipse.keyple.core.service.exception.KeypleReaderException
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.example.calypso.android.nfc.R
import org.eclipse.keyple.example.util.CalypsoClassicInfo
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcProtocolSettings
import timber.log.Timber

class CoreExamplesActivity : AbstractExampleActivity() {

    override fun onResume() {
        super.onResume()
        reader.startCardDetection(ObservableReader.PollingMode.REPEATING)
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
        if (reader.isCardPresent) {
            /*
              * operate card AID selection (change the AID prefix here to adapt it to the card used for
              * the test [the card should have at least two applications matching the AID prefix])
              */
            val cardAidPrefix = CalypsoClassicInfo.AID_PREFIX

            /* First selection case */
            cardSelection = CardSelection()

            /* AID based selection (1st selection, later indexed 0) */
            cardSelection.prepareSelection(
                    GenericCardSelectionRequest(
                            CardSelector.builder()
                                    .cardProtocol(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name))
                                    .aidSelector(AidSelector.builder()
                                            .aidToSelect(cardAidPrefix)
                                            .fileOccurrence(AidSelector.FileOccurrence.FIRST)
                                            .fileControlInformation(AidSelector.FileControlInformation.FCI)
                                            .build())
                                    .build()))

            /* Do the selection and display the result */
            addActionEvent("FIRST MATCH Calypso PO selection for prefix: $cardAidPrefix")
            doAndAnalyseSelection(reader, cardSelection, 1)

            /*
              * New selection: get the next application occurrence matching the same AID, close the
              * physical channel after
              */
            cardSelection = CardSelection()

            /* Close the channel after the selection */
            cardSelection.prepareReleaseChannel()

            /* next selection (2nd selection, later indexed 1) */
            cardSelection.prepareSelection(
                    GenericCardSelectionRequest(
                            CardSelector.builder()
                                    .cardProtocol(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name))
                                    .aidSelector(AidSelector.builder()
                                            .aidToSelect(cardAidPrefix)
                                            .fileOccurrence(AidSelector.FileOccurrence.NEXT)
                                            .fileControlInformation(AidSelector.FileControlInformation.FCI)
                                            .build())
                                    .build()))

            /* Do the selection and display the result */
            addActionEvent("NEXT MATCH Calypso PO selection for prefix: $cardAidPrefix")
            doAndAnalyseSelection(reader, cardSelection, 2)
        } else {
            addResultEvent("No cards were detected.")
        }
        eventRecyclerView.smoothScrollToPosition(events.size - 1)
    }

    private fun doAndAnalyseSelection(reader: Reader, cardSelection: CardSelection, index: Int) {
        try {
            val selectionsResult = cardSelection.processExplicitSelection(reader)
            if (selectionsResult.hasActiveSelection()) {
                val smartCard = selectionsResult.activeSmartCard
                addResultEvent(getSmardCardInfos(smartCard, index))
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

        if (reader.isCardPresent) {
            cardSelection = CardSelection(MultiSelectionProcessing.PROCESS_ALL)

            /* Close the channel after the selection to force the selection of all applications */
            cardSelection.prepareReleaseChannel()

            /* operate card selection (change the AID here to adapt it to the card used for the test) */
            val cardAidPrefix = CalypsoClassicInfo.AID_PREFIX

            /* AID based selection (1st selection, later indexed 0) */
            cardSelection.prepareSelection(
                    GenericCardSelectionRequest(
                            CardSelector.builder()
                                    .cardProtocol(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name))
                                    .aidSelector(AidSelector.builder()
                                            .aidToSelect(cardAidPrefix)
                                            .fileOccurrence(AidSelector.FileOccurrence.FIRST)
                                            .fileControlInformation(AidSelector.FileControlInformation.FCI)
                                            .build())
                                    .build()))

            /* next selection (2nd selection, later indexed 1) */
            cardSelection.prepareSelection(
                    GenericCardSelectionRequest(
                            CardSelector.builder()
                                    .cardProtocol(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name))
                                    .aidSelector(AidSelector.builder()
                                            .aidToSelect(cardAidPrefix)
                                            .fileOccurrence(AidSelector.FileOccurrence.NEXT)
                                            .fileControlInformation(AidSelector.FileControlInformation.FCI)
                                            .build())
                                    .build()))

            /* next selection (3rd selection, later indexed 2) */
            cardSelection.prepareSelection(
                    GenericCardSelectionRequest(
                            CardSelector.builder()
                                    .cardProtocol(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name))
                                    .aidSelector(AidSelector.builder()
                                            .aidToSelect(cardAidPrefix)
                                            .fileOccurrence(AidSelector.FileOccurrence.NEXT)
                                            .fileControlInformation(AidSelector.FileControlInformation.FCI)
                                            .build())
                                    .build()))

            addActionEvent("Calypso PO selection for prefix: $cardAidPrefix")

            /*
            * Actual card communication: operate through a single request the card selection
            */
            try {
                val selectionResult = cardSelection.processExplicitSelection(reader)

                if (selectionResult.smartCards.isNotEmpty()) {
                    selectionResult.smartCards.forEach {
                        addResultEvent(getSmardCardInfos(it.value, it.key))
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
        cardSelection = CardSelection()

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
        val cardSelector = GenericCardSelectionRequest(CardSelector.builder()
                .cardProtocol(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name))
                .aidSelector(
                        AidSelector.builder()
                                .aidToSelect(aid)
                                .build())
                .build())

        /*
        * Add the selection case to the current selection (we could have added other cases here)
        */
        cardSelection.prepareSelection(cardSelector)

        /*
         * Provide the Reader with the selection operation to be processed when a card is inserted.
         */
        (reader as ObservableReader).setDefaultSelectionRequest(cardSelection.selectionOperation,
                ObservableReader.NotificationMode.MATCHED_ONLY, ObservableReader.PollingMode.REPEATING)

        // (reader as ObservableReader).addObserver(this) //ALready done in onCreate

        addActionEvent("Waiting for a card... The default AID based selection to be processed as soon as the card is detected.")

        useCase = object : UseCase {
            override fun onEventUpdate(event: ReaderEvent?) {
                CoroutineScope(Dispatchers.Main).launch {
                    when (event?.eventType) {
                        ReaderEvent.EventType.CARD_MATCHED -> {
                            addResultEvent("CARD_MATCHED event: A card corresponding to request has been detected")
                            val selectedCard = cardSelection.processDefaultSelection(event.defaultSelectionsResponse).activeSmartCard
                            if (selectedCard != null) {
                                addResultEvent("Observer notification: the selection of the card has succeeded. End of the card processing.")
                                addResultEvent("Application FCI = ${ByteArrayUtil.toHex(selectedCard.fciBytes)}")
                            } else {
                                addResultEvent("The selection of the card has failed. Should not have occurred due to the MATCHED_ONLY selection mode.")
                            }
                        }

                        ReaderEvent.EventType.CARD_INSERTED -> {
                            addResultEvent("CARD_INSERTED event: should not have occurred due to the MATCHED_ONLY selection mode.")
                        }

                        ReaderEvent.EventType.CARD_REMOVED -> {
                            addResultEvent("CARD_REMOVED event: There is no PO inserted anymore. Return to the waiting state...")
                        }

                        else -> {
                        }
                    }
                    eventRecyclerView.smoothScrollToPosition(events.size - 1)
                }
                if (event?.eventType == ReaderEvent.EventType.CARD_INSERTED || event?.eventType == ReaderEvent.EventType.CARD_MATCHED) {
                    /*
                     * Informs the underlying layer of the end of the card processing, in order to manage the
                     * removal sequence. <p>If closing has already been requested, this method will do
                     * nothing.
                     */
                    try {
                        (event.reader as ObservableReader).finalizeCardProcessing()
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

        if (reader.isCardPresent) {

            /*
             * Prepare the card selection
             */
            cardSelection = CardSelection()

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

            val genericCardSelectionRequest = GenericCardSelectionRequest(
                    CardSelector.builder()
                            .cardProtocol(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name))
                            .aidSelector(
                                    AidSelector.builder()
                                            .aidToSelect(aid)
                                            .build())
                            .build())

            /**
             * Prepare Selection
             */
            cardSelection.prepareSelection(genericCardSelectionRequest)

            /*
             * Provide the Reader with the selection operation to be processed when a card is inserted.
             */
            (reader as ObservableReader).setDefaultSelectionRequest(cardSelection.selectionOperation,
                    ObservableReader.NotificationMode.MATCHED_ONLY, ObservableReader.PollingMode.SINGLESHOT)

            /**
             * We won't be listening for event update within this use case
             */
            useCase = null

            addActionEvent("Calypso PO selection: $aid")
            try {
                val selectionsResult = cardSelection.processExplicitSelection(reader)

                if (selectionsResult.hasActiveSelection()) {
                    val matchedCard = selectionsResult.activeSmartCard
                    addResultEvent("The selection of the card has succeeded.")
                    addResultEvent("Application FCI = ${ByteArrayUtil.toHex(matchedCard.fciBytes)}")
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
     * Create a new class extending AbstractCardSelectionRequest
     */
    inner class GenericCardSelectionRequest(cardSelector: CardSelector) : AbstractCardSelectionRequest<AbstractApduCommandBuilder>(cardSelector) {
        override fun parse(CardSelectionResponse: CardSelectionResponse): AbstractSmartCard {
            class GenericSmartCard(
                    CardSelectionResponse: CardSelectionResponse
            ) : AbstractSmartCard(CardSelectionResponse)
            return GenericSmartCard(CardSelectionResponse)
        }
    }
}
