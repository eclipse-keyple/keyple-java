package org.eclipse.keyple.plugin.android.omapi.simalliance

import android.content.Context
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin
import org.simalliance.openmobileapi.Reader
import org.simalliance.openmobileapi.SEService
import timber.log.Timber

object AndroidOmapiPluginImpl: AndroidOmapiPlugin<Reader, SEService>(), SEService.CallBack {

    override fun connectToSe(context: Context) {
        val seServiceFactory = SeServiceFactoryImpl(context)
        seService = seServiceFactory.connectToSe(this)
        Timber.i("OMAPI SEService version: %s", seService?.version)
    }

    override fun getNativeReaders(): Array<Reader>? {
        return seService?.readers
    }

    override fun mapToSeReader(nativeReader: Reader): SeReader {
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
    override fun serviceConnected(p0: SEService?) {
        Timber.i("Retrieve available readers...")

        // init readers
        readers = initNativeReaders()
    }

}