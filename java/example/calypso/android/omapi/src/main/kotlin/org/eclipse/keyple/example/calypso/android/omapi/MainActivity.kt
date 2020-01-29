package org.eclipse.keyple.example.calypso.android.omapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.*
import org.eclipse.keyple.core.seproxy.SeProxyService
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstantiationException
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPluginFactory
import timber.log.Timber
import java.util.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object{
        private const val MAX_TRIES = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initActionBar()

        /* Get the instance of the SeProxyService (Singleton pattern) */
        val seProxyService = SeProxyService.getInstance()

        /* register Omapi Plugin to the SeProxyService */
        try {
            seProxyService.registerPlugin(AndroidOmapiPluginFactory(this))
        } catch (e: KeyplePluginInstantiationException) {
            e.printStackTrace()
        }

    }

    override fun onResume() {
        super.onResume()
        //Retrieve OMAPI Readers, can't observed but may take time.
        //So we retry every second 10 times
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch{
            try {
                val readers = connectOmapi()
                //readers are initialized, we can show the menu
                connectOmapiTV.visibility = View.GONE
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

    private fun initActionBar(){
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = "Hello APP"
        actionBar?.subtitle = "App subtitle"
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.setLogo(R.mipmap.ic_launcher)
        actionBar?.setDisplayUseLogoEnabled(true)
    }


    @Throws(KeyplePluginNotFoundException::class, KeyplePluginInstantiationException::class)
    private suspend fun connectOmapi(): SortedSet<SeReader> = withContext(Dispatchers.IO) {
        var readers : SortedSet<SeReader>? = null
        for(x in 1..MAX_TRIES){
            readers = SeProxyService.getInstance()
                    .getPlugin(AndroidOmapiPlugin.PLUGIN_NAME).readers
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

    private fun showAlertDialog(t: Throwable){
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle(R.string.alert_dialog_title)
        builder.setMessage(getString(R.string.alert_dialog_message, t.message))
        val dialog = builder.create()
        dialog.show()

    }
}
