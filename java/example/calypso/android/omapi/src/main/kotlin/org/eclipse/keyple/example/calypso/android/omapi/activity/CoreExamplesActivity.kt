package org.eclipse.keyple.example.calypso.android.omapi.activity

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_core_examples.*
import kotlinx.android.synthetic.main.activity_main.toolbar
import org.eclipse.keyple.core.selection.SeSelection
import org.eclipse.keyple.core.seproxy.ChannelControl
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException
import org.eclipse.keyple.core.seproxy.message.ProxyReader
import org.eclipse.keyple.core.seproxy.message.SeRequest
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.example.calypso.android.omapi.R
import org.eclipse.keyple.example.calypso.android.omapi.utils.AidEnum
import org.eclipse.keyple.example.calypso.android.omapi.utils.GenericSeSelectionRequest

/**
 * Activity execution Keple-Core based examples.
 */
class CoreExamplesActivity : ExamplesActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_core_examples)
        initActionBar(toolbar,"keyple-core", "Shows usage of Keyple Core")

        /**
         * Set event window
         */
        eventRecyclerView.layoutManager = layoutManager
        eventRecyclerView.adapter = adapter

        /**
         * Init Menu buttons
         */
        getReadersInfosButton.setOnClickListener(this)
        explicitSelectionAidButton.setOnClickListener(this)
        groupedMultiselectionButton.setOnClickListener(this)
        sequentialMultiSelectionButton.setOnClickListener(this)

        //What to do with core?
    }

    override fun onResume() {
        super.onResume()
        clearEvents()
    }

    private fun getReadersInfos(){
        addHeaderEvent("Readers found ${readers.size}, getting infos")

        readers.forEach {

            addActionEvent("Get reader name [reader.name]")
            val name = it.name
            addResultEvent("Reader name: [$name]")

            addActionEvent("Check First Reader Presency [reader.isSePresent]")
            val isSePresent = it.isSePresent
            addResultEvent("ReaderIsPresent: [$isSePresent]")
        }

        eventRecyclerView.smoothScrollToPosition(events.size-1)

    }

    private fun explicitSectionAid(){

        addHeaderEvent("UseCase Generic #1: AID based explicit selection")

        val aids = AidEnum.values().map { it.name }

        addChoiceEvent("Choose an Application:", aids) { selectedApp ->
            val poAid = AidEnum.valueOf(selectedApp).aid

            if(readers.size <1) {
                addResultEvent("No readers available")
            }else{
                readers.forEach {
                    addHeaderEvent("Starting explicitAidSelection with $poAid on Reader ${it.name}")

                    val seSelector = SeSelector(SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                            SeSelector.AidSelector(SeSelector.AidSelector.IsoAid(poAid), setOf(36864)), null)
                    val seRequest = SeRequest(seSelector, null)

                    addActionEvent("Sending SeRequest to select: $poAid")
                    try{
                        val seResponse = (it as ProxyReader).transmit(seRequest)

                        if(seResponse?.selectionStatus?.hasMatched() == true){
                            addResultEvent("The selection of the PO has succeeded.")
                            addResultEvent("Application FCI = ${ByteArrayUtil.toHex(seResponse.selectionStatus.fci.bytes)}")
                        }else{
                            addResultEvent("The selection of the PO Failed")
                        }
                    }catch (e: Exception){
                        addResultEvent("The selection of the PO Failed: ${e.message}")
                    }
                }
            }

            eventRecyclerView.smoothScrollToPosition(events.size-1)
        }
        eventRecyclerView.smoothScrollToPosition(events.size-1)

    }

    /**
     * Next may not be supported, depends on OMAPI implementation
     */
    private fun groupedMultiSelection(){
        addHeaderEvent("UseCase Generic #3: AID based grouped explicit multiple selection")

        /* CLOSE_AFTER in order to secure selection of all applications */
        val seSelection = SeSelection(MultiSeRequestProcessing.PROCESS_ALL,
                ChannelControl.CLOSE_AFTER)

        /* operate SE selection (change the AID here to adapt it to the SE used for the test) */
        val seAidPrefix = "A000000404012509"

        /* AID based selection (1st selection, later indexed 0) */
        seSelection.prepareSelection(GenericSeSelectionRequest(SeSelector(
                SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                SeSelector.AidSelector(SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                        SeSelector.AidSelector.FileOccurrence.FIRST,
                        SeSelector.AidSelector.FileControlInformation.FCI),
                "Initial selection #1")))

        /* next selection (2nd selection, later indexed 1) */
        seSelection.prepareSelection(GenericSeSelectionRequest(SeSelector(
                SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                SeSelector.AidSelector(SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                        SeSelector.AidSelector.FileOccurrence.NEXT,
                        SeSelector.AidSelector.FileControlInformation.FCI),
                "Next selection #2")))

        /* next selection (3rd selection, later indexed 2) */
        seSelection.prepareSelection(GenericSeSelectionRequest(SeSelector(
                SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                SeSelector.AidSelector(SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                        SeSelector.AidSelector.FileOccurrence.NEXT,
                        SeSelector.AidSelector.FileControlInformation.FCI),
                "Next selection #3")))

        /*
         * Actual SE communication: operate through a single request the SE selection
         */
        if(readers.size <1) {
            addResultEvent("No readers available")
        }else{
            readers.forEach {seReader: SeReader ->
                if(seReader.isSePresent){
                    addActionEvent("Sending multiSelection request based on AID Prefix $seAidPrefix to ${seReader.name}")
                    try{
                        val selectionsResult = seSelection.processExplicitSelection(seReader)
                        if(selectionsResult.matchingSelections.size>0){
                            selectionsResult.matchingSelections.forEach {
                                val matchingSe = it.matchingSe
                                addResultEvent("Selection status for selection ${it.extraInfo} " +
                                        "(indexed ${it.selectionIndex}): \n\t\t" +
                                        "ATR: ${ByteArrayUtil.toHex(matchingSe.selectionStatus.atr.bytes)}\n\t\t" +
                                        "FCI: ${ByteArrayUtil.toHex(matchingSe.selectionStatus.fci.dataOut)}")
                            }
                        }else {
                            addResultEvent("No SE matched the selection.")
                        }
                    }catch (e: Exception){
                        addResultEvent("The selection of the PO Failed: ${e.message}")
                    }
                }else{
                    addResultEvent("No SE were detected")
                }
            }
        }
    }

    /**
     * Next may not be supported, depends on OMAPI implementation
     */
    private fun sequentialMultiSelection(){
        addHeaderEvent("UseCase Generic #4: AID based sequential explicit multiple selection")

        /* operate SE selection (change the AID here to adapt it to the SE used for the test) */
        val seAidPrefix = "A000000404012509"

        if(readers.size <1) {
            addResultEvent("No readers available")
        }else{
            readers.forEach { seReader: SeReader ->
                if (seReader.isSePresent) {

                    var seSelection = SeSelection(MultiSeRequestProcessing.FIRST_MATCH,
                            ChannelControl.KEEP_OPEN)

                    /*
                     * AID based selection: get the first application occurrence matching the AID, keep the
                     * physical channel open
                     */
                    seSelection.prepareSelection(GenericSeSelectionRequest(SeSelector(
                            SeCommonProtocols.PROTOCOL_ISO14443_4,
                            null,
                            SeSelector.AidSelector(
                                    SeSelector.AidSelector.IsoAid(ByteArrayUtil.fromHex(seAidPrefix)), null, SeSelector.AidSelector.FileOccurrence.FIRST,
                                    SeSelector.AidSelector.FileControlInformation.FCI),
                            "Initial selection #1")))
                    /* Do the selection and display the result */
                    doAndAnalyseSelection(seReader, seSelection, 1, seAidPrefix)

                    /*
                     * New selection: get the next application occurrence matching the same AID, close the
                     * physical channel after
                     */
                    seSelection = SeSelection(MultiSeRequestProcessing.FIRST_MATCH,
                            ChannelControl.CLOSE_AFTER)

                    seSelection.prepareSelection(GenericSeSelectionRequest(SeSelector(
                            SeCommonProtocols.PROTOCOL_ISO14443_4,
                            null,
                            SeSelector.AidSelector(
                                    SeSelector.AidSelector.IsoAid(ByteArrayUtil.fromHex(seAidPrefix)), null, SeSelector.AidSelector.FileOccurrence.NEXT,
                                    SeSelector.AidSelector.FileControlInformation.FCI),
                            "Next selection #2")))

                    /* Do the selection and display the result */
                    doAndAnalyseSelection(seReader, seSelection, 2, seAidPrefix)
                }else{
                    addResultEvent("No SE were detected")
                }
            }
        }
    }

    @Throws(KeypleReaderException::class)
    private fun doAndAnalyseSelection(seReader: SeReader, seSelection: SeSelection, index: Int, seAidPrefix: String) {
        addActionEvent("Sending multiSelection request based on AID Prefix $seAidPrefix to ${seReader.name}")
        val selectionsResult = seSelection.processExplicitSelection(seReader)
        if (selectionsResult.hasActiveSelection()) {
            val matchingSe = selectionsResult.getMatchingSelection(0).matchingSe
            addResultEvent("The SE matched the selection $index.")

            addResultEvent("Selection status for case $index: \n\t\t" +
                    "ATR: ${ByteArrayUtil.toHex(matchingSe.selectionStatus.atr.bytes)}\n\t\t" +
                    "FCI: ${ByteArrayUtil.toHex(matchingSe.selectionStatus.fci.dataOut)}")
        } else {
            addResultEvent("The selection did not match for case $index.")
        }
    }


    override fun onClick(v: View?) {
        v?.let {
            when(it.id){
                getReadersInfosButton.id -> getReadersInfos()
                explicitSelectionAidButton.id -> explicitSectionAid()
                groupedMultiselectionButton.id -> groupedMultiSelection()
                sequentialMultiSelectionButton.id -> sequentialMultiSelection()
            }
        }
    }


}
