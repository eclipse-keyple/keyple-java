package org.eclipse.keyple.plugin.android.omapi.simalliance

import android.content.Context
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin
import org.simalliance.openmobileapi.Reader
import org.simalliance.openmobileapi.SEService
import timber.log.Timber

object AndroidSaOmapiPluginImpl: AndroidOmapiPlugin<Reader, SEService>(), SEService.CallBack {

    override fun connectToSe(context: Context) {
        val seServiceFactory = SeServiceFactoryImpl()
        seService = seServiceFactory.connectToSe(this)
        Timber.i("OMAPI SEService version: %s", seService?.version)
    }

    override fun getNativeReaders(): Array<Reader>? {
        return seService?.readers
    }

    override fun mapToSeReader(reader: Reader): SeReader {
        Timber.d("Reader available name : %s", reader.name)
        Timber.d("Reader available isSePresent : %S", reader.isSecureElementPresent)
        return AndroidOmapiReaderImpl(PLUGIN_NAME, reader, reader.name)
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