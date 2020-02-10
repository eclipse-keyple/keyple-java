package org.eclipse.keyple.example.calypso.android.omapi.activity

import android.view.MenuItem
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_calypso_example.*
import kotlinx.android.synthetic.main.activity_calypso_example.drawerLayout
import kotlinx.android.synthetic.main.activity_core_examples.*
import kotlinx.android.synthetic.main.activity_core_examples.eventRecyclerView
import kotlinx.android.synthetic.main.activity_main.toolbar
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest
import org.eclipse.keyple.calypso.transaction.PoSelector
import org.eclipse.keyple.core.selection.SeSelection
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.example.calypso.android.omapi.R
import org.eclipse.keyple.example.calypso.android.omapi.utils.AidEnum

class CalypsoExamplesActivity : ExamplesActivity() {

    override fun initContentView() {
        setContentView(R.layout.activity_calypso_example)
        initActionBar(toolbar,"keyple-calypso", "Shows usage of Keyple Calypso")
    }

    private fun explicitSectionAid(){
        val poAid = AidEnum.NAVIGO2013.aid //navigo (without version number 01)
        addHeaderEvent("Starting explicitAidSelection with: %s".format(poAid))

        /*
         * Get a reader
         */
        val seReader = readers.first()

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

        addActionEvent("Process explicit selection")
        val selectionsResult = seSelection.processExplicitSelection(seReader)

        if(selectionsResult.hasActiveSelection()){
            val matchedSe = selectionsResult.activeSelection.matchingSe
            addResultEvent("The selection of the SE has succeeded.")
            addResultEvent("Application FCI = %s".format(ByteArrayUtil.toHex(matchedSe.selectionStatus.fci.bytes)))
        }else{
            addResultEvent("The selection of the PO Failed")
            showAlertDialog(NoSuchElementException("Could not select: %s".format(poAid)))
        }

        eventRecyclerView.smoothScrollToPosition(events.size-1)
    }

    private fun readEnvironmentAndUsage(){
        readEnvironmentAndUsage(AidEnum.NAVIGO2013)
        eventRecyclerView.smoothScrollToPosition(events.size-1)
    }

    private fun readEnvironmentAndUsage(aidEnum: AidEnum){

        val reader=readers.first()

        if(aidEnum== AidEnum.NAVIGO2013){

            val poAid = AidEnum.NAVIGO2013.aid
            val sfiNavigoEFEnvironment = 0x07.toByte()
            val sfiNavigoEFTransportEvent = 0x08.toByte()

            addHeaderEvent("Starting readEnvironmentAndUsage with: %s".format(poAid))

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
            val readEnvironmentParserIndex = poSelectionRequest.prepareReadRecordsCmd(
                    sfiNavigoEFEnvironment, ReadDataStructure.SINGLE_RECORD_DATA,
                    1.toByte(), 29, String.format("Navigo2013 EF environment (SFI=%02X)",
                    sfiNavigoEFEnvironment))

            val readTransportEventParserIndex = poSelectionRequest.prepareReadRecordsCmd(
                    sfiNavigoEFTransportEvent, ReadDataStructure.SINGLE_RECORD_DATA,
                    1.toByte(), 29, String.format("Navigo2013 EF TransportEvent (SFI=%02X)",
                    sfiNavigoEFTransportEvent))

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
            addActionEvent("Process explicit selection for %s and reading Environment and transport event".format(poAid))
            val selectionsResult = seSelection.processExplicitSelection(reader)

            if (selectionsResult.hasActiveSelection()) {
                val matchingSelection = selectionsResult.activeSelection

                //val calypsoPo = matchingSelection.matchingSe as CalypsoPo
                addResultEvent("Selection succeeded for P0 with aid %s".format(poAid))

                val readEnvironmentParser = matchingSelection
                        .getResponseParser(readEnvironmentParserIndex) as ReadRecordsRespPars

                /*
                 * Retrieve the data read from the parser updated during the selection
                 * process (Environment)
                 */
                val environmentAndHolder = readEnvironmentParser.records[1]
                addResultEvent("Environment file data: %s".format(ByteArrayUtil.toHex(environmentAndHolder)))


                val readTransportEventParser = matchingSelection
                        .getResponseParser(readTransportEventParserIndex) as ReadRecordsRespPars

                /*
                 * Retrieve the data read from the parser updated during the selection
                 * process (Usage)
                 */
                val transportEvents = readTransportEventParser.records[1]
                addResultEvent("Transport Event file data: %s".format(ByteArrayUtil.toHex(transportEvents)))

            }
        }else if(aidEnum== AidEnum.HOPLINK){

        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.explicitSelectionAidButton -> explicitSectionAid()
            R.id.readEnvironnementAndUsageButton -> readEnvironmentAndUsage()
        }
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        return true
    }
}
