package org.eclipse.keyple.example.calypso.android.omapi.activity

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_core_examples.*
import kotlinx.android.synthetic.main.activity_main.toolbar
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.message.ProxyReader
import org.eclipse.keyple.core.seproxy.message.SeRequest
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.example.calypso.android.omapi.R
import org.eclipse.keyple.example.calypso.android.omapi.utils.AidEnum
import java.util.*


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

        //What to do with core?
    }

    override fun onResume() {
        super.onResume()
        clearEvents()
    }

    private fun getReadersInfos(){
        addHeaderEvent("Readers found %d, getting infos".format(2))

        readers.forEach {

            addActionEvent("Get reader name [reader.name]")
            val name = it.name
            addResultEvent("Reader name: [%s]".format(name))

            addActionEvent("Check First Reader Presency [reader.isSePresent]")
            val isSePresent = it.isSePresent
            if(isSePresent){
                addResultEvent("FirstReaderIsPresent [%s]".format(isSePresent))
            }else{
                addResultEvent("FirstReaderIsNotPresent[%s]".format(isSePresent))
            }
        }

        eventRecyclerView.smoothScrollToPosition(events.size-1)

    }

    private fun explicitSectionAid(){
        val poAid = AidEnum.NAVIGO2013.aid //navigo (without version number 01)

        addHeaderEvent("Starting explicitAidSelection with: %s".format(poAid))

        val seReader = readers.first() as ProxyReader
        val seSelector = SeSelector(SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                SeSelector.AidSelector(SeSelector.AidSelector.IsoAid("A00000040401250901"), setOf(36864)), null)
        val seRequest = SeRequest(seSelector, null)

        addActionEvent("Sending SeRequest to select: %s".format(poAid))
        val seResponse = seReader.transmit(seRequest)

        if(seResponse.selectionStatus.hasMatched()){
            addResultEvent("The selection of the PO has succeeded.")
            addResultEvent("Application FCI = %s".format(ByteArrayUtil.toHex(seResponse.selectionStatus.fci.bytes)))
        }else{
            addResultEvent("The selection of the PO Failed")
            showAlertDialog(NoSuchElementException("Could not select: %s".format(poAid)))
        }
        eventRecyclerView.smoothScrollToPosition(events.size-1)
    }

    override fun onClick(v: View?) {
        v?.let {
            when(it.id){
                getReadersInfosButton.id -> getReadersInfos()
                explicitSelectionAidButton.id -> explicitSectionAid()
            }
        }
    }


}
