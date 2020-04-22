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
import org.eclipse.keyple.core.selection.AbstractMatchingSe
import org.eclipse.keyple.core.selection.AbstractSeSelectionRequest
import org.eclipse.keyple.core.selection.SeSelection
import org.eclipse.keyple.core.seproxy.ChannelControl
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing
import org.eclipse.keyple.core.seproxy.SeProxyService
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.event.ObservableReader
import org.eclipse.keyple.core.seproxy.event.ReaderEvent
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException
import org.eclipse.keyple.core.seproxy.message.SeResponse
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode
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
        addHeaderEvent("SE Reader  NAME = ${reader.name}")

        /*Check if a SE is present in the reader */
        if (reader.isSePresent) {
            /*
              * operate SE AID selection (change the AID prefix here to adapt it to the SE used for
              * the test [the SE should have at least two applications matching the AID prefix])
              */
            val seAidPrefix = CalypsoClassicInfo.AID_PREFIX

            /* First selection case */
            seSelection = SeSelection(MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.KEEP_OPEN)

            /* AID based selection (1st selection, later indexed 0) */
            seSelection.prepareSelection(
                    GenericSeSelectionRequest(
                            SeSelector(
                                    SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                                    SeSelector.AidSelector(SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                                            SeSelector.AidSelector.FileOccurrence.FIRST,
                                            SeSelector.AidSelector.FileControlInformation.FCI))))

            /* Do the selection and display the result */
            addActionEvent("FIRST MATCH Calypso PO selection for prefix: $seAidPrefix")
            doAndAnalyseSelection(reader, seSelection, 1)

            /*
              * New selection: get the next application occurrence matching the same AID, close the
              * physical channel after
              */
            seSelection = SeSelection(MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER)

            /* next selection (2nd selection, later indexed 1) */
            seSelection.prepareSelection(
                    GenericSeSelectionRequest(
                            SeSelector(
                                    SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                                    SeSelector.AidSelector(SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                                            SeSelector.AidSelector.FileOccurrence.NEXT,
                                            SeSelector.AidSelector.FileControlInformation.FCI))))

            /* Do the selection and display the result */
            addActionEvent("NEXT MATCH Calypso PO selection for prefix: $seAidPrefix")
            doAndAnalyseSelection(reader, seSelection, 2)
        } else {
            addResultEvent("No SE were detected.")
        }
        eventRecyclerView.smoothScrollToPosition(events.size - 1)
    }

    private fun doAndAnalyseSelection(reader: SeReader, seSelection: SeSelection, index: Int) {
        try {
            val selectionsResult = seSelection.processExplicitSelection(reader)
            if (selectionsResult.hasActiveSelection()) {
                with(selectionsResult.getMatchingSelection(0)) {
                    val matchingSe = this.matchingSe
                    addResultEvent("Selection status for selection " +
                            "(indexed ${this.selectionIndex}): \n\t\t" +
                            "ATR: ${ByteArrayUtil.toHex(matchingSe.selectionStatus.atr.bytes)}\n\t\t" +
                            "FCI: ${ByteArrayUtil.toHex(matchingSe.selectionStatus.fci.bytes)}")
                }
            } else {
                addResultEvent("The selection did not match for case $index.")
            }
        } catch (e: KeypleReaderException) {
            addResultEvent("Error: ${e.message}")
        }
    }

    private fun configureUseCase3GroupedMultiSelection() {
        addHeaderEvent("UseCase Generic #3: AID based grouped explicit multiple selection")
        addHeaderEvent("SE Reader  NAME = ${reader.name}")

        useCase = null

        if (reader.isSePresent) {
            /* CLOSE_AFTER to force selection of all applications*/
            seSelection = SeSelection(MultiSeRequestProcessing.PROCESS_ALL, ChannelControl.CLOSE_AFTER)

            /* operate SE selection (change the AID here to adapt it to the SE used for the test) */
            val seAidPrefix = CalypsoClassicInfo.AID_PREFIX

            /* AID based selection (1st selection, later indexed 0) */
            seSelection.prepareSelection(
                    GenericSeSelectionRequest(
                            SeSelector(
                                    SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                                    SeSelector.AidSelector(SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                                            SeSelector.AidSelector.FileOccurrence.FIRST,
                                            SeSelector.AidSelector.FileControlInformation.FCI))))

            /* next selection (2nd selection, later indexed 1) */
            seSelection.prepareSelection(
                    GenericSeSelectionRequest(
                            SeSelector(
                                    SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                                    SeSelector.AidSelector(SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                                            SeSelector.AidSelector.FileOccurrence.NEXT,
                                            SeSelector.AidSelector.FileControlInformation.FCI))))

            /* next selection (3rd selection, later indexed 2) */
            seSelection.prepareSelection(
                    GenericSeSelectionRequest(
                            SeSelector(
                                    SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                                    SeSelector.AidSelector(SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                                            SeSelector.AidSelector.FileOccurrence.NEXT,
                                            SeSelector.AidSelector.FileControlInformation.FCI))))

            addActionEvent("Calypso PO selection for prefix: $seAidPrefix")

            /*
            * Actual SE communication: operate through a single request the SE selection
            */
            try {
                val selectionResult = seSelection.processExplicitSelection(reader)

                if (selectionResult.matchingSelections.size > 0) {
                    selectionResult.matchingSelections.forEach {
                        val matchingSe = it.matchingSe
                        addResultEvent("Selection status for selection " +
                                "(indexed ${it.selectionIndex}): \n\t\t" +
                                "ATR: ${ByteArrayUtil.toHex(matchingSe.selectionStatus.atr.bytes)}\n\t\t" +
                                "FCI: ${ByteArrayUtil.toHex(matchingSe.selectionStatus.fci.bytes)}")
                    }
                    addResultEvent("End of selection")
                } else {
                    addResultEvent("No SE matched the selection.")
                    addResultEvent("SE must be in the field when starting this use case")
                }
            } catch (e: KeypleReaderException) {
                addResultEvent("Error: ${e.message}")
            }
        } else {
            addResultEvent("No SE were detected.")
        }

        eventRecyclerView.smoothScrollToPosition(events.size - 1)
    }

    private fun configureUseCase2DefaultSelectionNotification() {
        addHeaderEvent("UseCase Generic #2: AID based default selection")
        addHeaderEvent("SE Reader  NAME = ${reader.name}")

        /*
         * Prepare a SE selection
         */
        seSelection = SeSelection()

        /*
        * Setting of an AID based selection
        *
        * Select the first application matching the selection AID whatever the SE communication
        * protocol keep the logical channel open after the selection
        */
        val aid = CalypsoClassicInfo.AID

        /*
         * Generic selection: configures a SeSelector with all the desired attributes to make the
         * selection
         */
        val seSelector = GenericSeSelectionRequest(SeSelector(
                SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                SeSelector.AidSelector(
                        SeSelector.AidSelector.IsoAid(ByteArrayUtil.fromHex(aid)), null)))

        /*
        * Add the selection case to the current selection (we could have added other cases here)
        */
        seSelection.prepareSelection(seSelector)

        /*
         * Provide the SeReader with the selection operation to be processed when a SE is inserted.
         */
        (reader as ObservableReader).setDefaultSelectionRequest(seSelection.selectionOperation,
                ObservableReader.NotificationMode.MATCHED_ONLY, ObservableReader.PollingMode.REPEATING)

        // (reader as ObservableReader).addObserver(this) //ALready done in onCreate

        addActionEvent("Waiting for a SE... The default AID based selection to be processed as soon as the SE is detected.")

        useCase = object : UseCase {
            override fun onEventUpdate(event: ReaderEvent?) {
                CoroutineScope(Dispatchers.Main).launch {
                    when (event?.eventType) {
                        ReaderEvent.EventType.SE_MATCHED -> {
                            addResultEvent("SE_MATCHED event: A SE corresponding to request has been detected")
                            val selectedSe = seSelection.processDefaultSelection(event.defaultSelectionsResponse).activeSelection.matchingSe
                            if (selectedSe != null) {
                                addResultEvent("Observer notification: the selection of the SE has succeeded. End of the SE processing.")
                                addResultEvent("Application FCI = ${ByteArrayUtil.toHex(selectedSe.selectionStatus.fci.bytes)}")
                            } else {
                                addResultEvent("The selection of the SE has failed. Should not have occurred due to the MATCHED_ONLY selection mode.")
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
                    /*
                     * Informs the underlying layer of the end of the SE processing, in order to manage the
                     * removal sequence. <p>If closing has already been requested, this method will do
                     * nothing.
                     */
                    try {
                        (SeProxyService.getInstance().getPlugin(event.pluginName).getReader(event.readerName) as ObservableReader).notifySeProcessed()
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
        addHeaderEvent("SE Reader  NAME = ${reader.name}")

        if (reader.isSePresent) {

            /*
             * Prepare the SE selection
             */
            seSelection = SeSelection()

            /*
             * Setting of an AID based selection (in this example a Calypso REV3 PO)
             *
             * Select the first application matching the selection AID whatever the SE communication
             * protocol keep the logical channel open after the selection
             */
            val aid = CalypsoClassicInfo.AID

            /*
             * Generic selection: configures a SeSelector with all the desired attributes to make
             * the selection and read additional information afterwards
             */
            val genericSeSelectionRequest = GenericSeSelectionRequest(
                    SeSelector(SeCommonProtocols.PROTOCOL_ISO14443_4,
                            null,
                            SeSelector.AidSelector(
                                    SeSelector.AidSelector.IsoAid(ByteArrayUtil.fromHex(aid)), null)))

            /**
             * Prepare Selection
             */
            seSelection.prepareSelection(genericSeSelectionRequest)

            /*
             * Provide the SeReader with the selection operation to be processed when a SE is inserted.
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
                    val matchedSe = selectionsResult.activeSelection.matchingSe
                    addResultEvent("The selection of the SE has succeeded.")
                    addResultEvent("Application FCI = ${ByteArrayUtil.toHex(matchedSe.selectionStatus.fci.bytes)}")
                    addResultEvent("End of the generic SE processing.")
                } else {
                    addResultEvent("The selection of the SE has failed.")
                }
            } catch (e: KeypleReaderException) {
                Timber.e(e)
                addResultEvent("Error: ${e.message}")
            }
        } else {
            addResultEvent("No SE were detected.")
            addResultEvent("SE must be in the field when starting this use case")
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
    inner class GenericSeSelectionRequest(seSelector: SeSelector) : AbstractSeSelectionRequest(seSelector) {
        private var transmissionMode: TransmissionMode = seSelector.seProtocol.transmissionMode

        override fun parse(seResponse: SeResponse): AbstractMatchingSe {
            class GenericMatchingSe(
                selectionResponse: SeResponse,
                transmissionMode: TransmissionMode
            ) : AbstractMatchingSe(selectionResponse, transmissionMode)
            return GenericMatchingSe(seResponse, transmissionMode)
        }
    }
}
