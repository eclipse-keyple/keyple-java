package org.eclipse.keyple.plugin.android.omapi.se

import android.content.Context
import android.se.omapi.SEService
import android.util.Log
import androidx.annotation.RequiresApi
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.core.seproxy.plugin.AbstractPlugin
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin
import java.util.*

@RequiresApi(android.os.Build.VERSION_CODES.P) //OS version providing android.se.omapi package
object AndroidSeOmapiPluginImpl: AbstractPlugin(AndroidOmapiPlugin.PLUGIN_NAME), AndroidOmapiPlugin, SEService.OnConnectedListener {

    private val TAG = AndroidSeOmapiPluginImpl::class.java.simpleName

    private var seService: SEService? = null

    fun init(context: Context): AndroidOmapiPlugin{
        Log.d(TAG, "Init")
        val seServiceFactory = SeServiceFactoryImpl(context.applicationContext)
        seService = seServiceFactory.connectToSe(this)
        Log.i(TAG, "OMAPI SEService version: " + seService?.version)
        return this
    }

    override fun initNativeReaders(): SortedSet<SeReader> {
        Log.d(TAG, "initNativeReaders")
        val readers = sortedSetOf<SeReader>()
        if (seService?.isConnected == true) {
            seService?.readers?.forEach {
                Log.d(TAG, "Reader available name : " + it.name)
                Log.d(TAG,
                        "Reader available isSePresent : " + it.isSecureElementPresent)
                val seReader = AndroidSeOmapiReaderImpl(AndroidOmapiPlugin.PLUGIN_NAME, it, it.name)
                readers.add(seReader)
            }

            return readers

        } else {
            Log.w(TAG, "OMAPI SeService is not connected yet")
            return readers// empty list
            //throw new KeypleReaderException("OMAPI SeService is not connected yet, try again");
            //can throw an exception to notif
        }
    }

    /**
     * Warning. Do not call this method directly.
     *
     * Invoked by Open Mobile {@link SEService} when connected
     * Instantiates {@link AndroidSeOmapiReaderImpl} for each SE Reader detected in the platform
     *
     * @param seService : connected omapi service
     */
    override fun onConnected() {

        Log.i(TAG, "Retrieve available readers...")

        // init readers
        readers = initNativeReaders()
    }

    private val parameters = mutableMapOf<String, String>()// not in use in this
    override fun getParameters(): MutableMap<String, String> {
        Log.w(TAG, "Android OMAPI Plugin does not support parameters, see OMAPINfcReader instead")
        return parameters
    }


    override fun setParameter(key: String, value: String) {
        Log.w(TAG, "Android OMAPI  Plugin does not support parameters, see OMAPINfcReader instead")
        parameters[key] = value
    }
}