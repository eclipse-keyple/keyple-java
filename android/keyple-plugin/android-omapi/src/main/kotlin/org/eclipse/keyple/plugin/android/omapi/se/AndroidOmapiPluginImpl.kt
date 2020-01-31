package org.eclipse.keyple.plugin.android.omapi.se

import android.content.Context
import android.se.omapi.Reader
import android.se.omapi.SEService
import androidx.annotation.RequiresApi
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin
import timber.log.Timber

@RequiresApi(android.os.Build.VERSION_CODES.P) //OS version providing android.se.omapi package
object AndroidOmapiPluginImpl: AndroidOmapiPlugin<Reader, SEService>(), SEService.OnConnectedListener {

    override fun connectToSe(context: Context) {
        val seServiceFactory = SeServiceFactoryImpl(context.applicationContext)
        seService = seServiceFactory.connectToSe(this)
        Timber.i("OMAPI SEService version: %s", seService?.version)
    }

    override fun getNativeReaders(): Array<Reader>?{
        return seService?.readers
    }

    override fun mapToSeReader(nativeReader: Reader): SeReader{
        Timber.d("Reader available name : %s", nativeReader.name)
        Timber.d("Reader available isSePresent : %S", nativeReader.isSecureElementPresent)
        return AndroidOmapiReaderImpl(nativeReader, PLUGIN_NAME, nativeReader.name)
    }

    /**
     * Warning. Do not call this method directly.
     *
     * Invoked by Open Mobile {@link SEService} when connected
     * Instantiates {@link AndroidOmapiReaderImpl} for each SE Reader detected in the platform
     *
     * @param seService : connected omapi service
     */
    override fun onConnected() {

        Timber.i("Retrieve available readers...")

        // init readers
        readers = initNativeReaders()
    }
}