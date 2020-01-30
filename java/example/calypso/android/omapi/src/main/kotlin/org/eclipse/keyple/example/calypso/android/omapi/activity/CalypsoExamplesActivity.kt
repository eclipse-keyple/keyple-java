package org.eclipse.keyple.example.calypso.android.omapi.activity

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.keyple.core.seproxy.SeProxyService
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.example.calypso.android.omapi.R
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin
import java.util.*

class CalypsoExamplesActivity : BasicActivity() {

    lateinit var readers : SortedSet<SeReader>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calypso_example)
        initActionBar(toolbar,"keyple-calypso", "Shows usage of Keyple Calypso")

        readers = SeProxyService.getInstance().getPlugin(AndroidOmapiPlugin.PLUGIN_NAME).readers
    }
}
