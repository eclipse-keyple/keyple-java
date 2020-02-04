package org.eclipse.keyple.example.calypso.android.omapi.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.eclipse.keyple.core.seproxy.SeProxyService
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstantiationException
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException
import org.eclipse.keyple.example.calypso.android.omapi.R
import org.eclipse.keyple.plugin.android.omapi.PLUGIN_NAME
import timber.log.Timber
import java.util.*

class MainActivity : BasicActivity(), View.OnClickListener {

    companion object{
        private const val MAX_TRIES = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initActionBar(toolbar,"OMAPI Plugin", "Examples application")

        coreExamplesButton.setOnClickListener(this)
        calypsoExampleButton.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        //Retrieve OMAPI Readers, can't observed but may take time.
        //So we retry every second 10 times
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch{
            try {
                connectOmapi()
                //readers are initialized, we can show the menu
                connectOmapiTV.visibility = View.GONE
                menuLayout.visibility = View.VISIBLE
            } catch (e: KeyplePluginNotFoundException) {
                Timber.e(e)
                showAlertDialog(e)
            } catch (e: KeyplePluginInstantiationException){
                Timber.e(e)
                showAlertDialog(e)
            }finally {
                progressBar.visibility = View.INVISIBLE
            }
        }
    }


    @Throws(KeyplePluginNotFoundException::class, KeyplePluginInstantiationException::class)
    private suspend fun connectOmapi(): SortedSet<SeReader> = withContext(Dispatchers.IO) {
        var readers : SortedSet<SeReader>? = null
        for(x in 1..MAX_TRIES){
            readers = SeProxyService.getInstance()
                    .getPlugin(PLUGIN_NAME).readers
            if(readers == null|| readers.size < 1) {
                Timber.d("No readers found in OMAPI Keyple Plugin")
                Timber.d("Retrying in 1 second")
                delay(1000)
            }else{
                Timber.d("Readers Found")
                break
            }
        }

        readers ?: throw KeyplePluginInstantiationException(getString(R.string.error_no_reader_found, MAX_TRIES))
    }

    override fun onClick(v: View?) {
        v?.let {
            when(it.id){
                R.id.calypsoExampleButton -> startActivity(Intent(this, CalypsoExamplesActivity::class.java))
                R.id.coreExamplesButton -> startActivity(Intent(this, CoreExamplesActivity::class.java))
                else -> {}
            }
        }
    }
}
