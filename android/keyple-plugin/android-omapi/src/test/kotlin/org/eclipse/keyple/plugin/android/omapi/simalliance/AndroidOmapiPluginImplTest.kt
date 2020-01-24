package org.eclipse.keyple.plugin.android.omapi.simalliance

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPluginTest
import org.simalliance.openmobileapi.Reader
import org.simalliance.openmobileapi.SEService

class AndroidOmapiPluginImplTest: AndroidOmapiPluginTest<Reader, SEService>() {

    override lateinit var androidOmapiPlugin: AndroidOmapiPlugin<Reader, SEService>

    override fun buildAndroidOmapiPlugin(context: Context): AndroidOmapiPluginImpl {
        return AndroidOmapiPluginImpl.init(context) as AndroidOmapiPluginImpl
    }

    override fun mockReader(name: String, isPresent: Boolean): Reader {
        val reader= mockk<Reader>()
        val seService= mockk<SEService>()
        val version = "3.2"
        every { reader.isSecureElementPresent } returns isPresent
        every { reader.name } returns name
        every { reader.seService } returns seService
        every { seService.version } returns version
        return reader
    }

    override fun mockGetNativeReaders(androidOmapiPlugin: AndroidOmapiPlugin<Reader, SEService>, readersToMock: Map<String, Boolean>) {
        mockkObject(androidOmapiPlugin)
        val readers = readersToMock.map { mockReader(it.key, it.value) }.toTypedArray()
        every { androidOmapiPlugin.getNativeReaders() } returns readers
    }

    override fun triggerOnConnected() {
        val seService= mockk<SEService>()
        (androidOmapiPlugin as AndroidOmapiPluginImpl).serviceConnected(seService)
    }
}