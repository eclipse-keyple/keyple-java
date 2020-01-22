package org.eclipse.keyple.plugin.android.omapi.se

import android.content.Context
import android.se.omapi.Reader
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class AndroidOmapiPluginImplTest {

    lateinit var context: Context
    lateinit var androidOmapiPlugin: AndroidOmapiPluginImpl

    @Before
    fun setUp() {
        context = mockContext()
        androidOmapiPlugin = AndroidOmapiPluginImpl.init(context) as AndroidOmapiPluginImpl
        mockkObject(androidOmapiPlugin)
        val readers = arrayOf(mockReader("SIM1", true), mockReader("SIM2", false))
        every { androidOmapiPlugin.getNativeReaders() } returns readers
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun connectToSe() {
        androidOmapiPlugin.connectToSe(context)
    }

    @Test
    fun getNativeReaders() {
        assertNotNull(androidOmapiPlugin.readers)
    }

    @Test
    fun mapToSeReader() {
        val reader1 = mockReader("SIM1", true)
        val seReader1 = androidOmapiPlugin.mapToSeReader(reader1)
        assertNotNull(seReader1)
        assertTrue(seReader1.isSePresent)
        assertEquals("SIM1", seReader1.name)

        val reader2 = mockReader("SIM2", false)
        val seReader2 = androidOmapiPlugin.mapToSeReader(reader2)
        assertNotNull(seReader2)
        assertFalse(seReader2.isSePresent)
        assertEquals("SIM2", seReader2.name)
    }

    @Test
    fun onConnected() {
        assertNotNull(androidOmapiPlugin.readers)
        assertEquals(0, androidOmapiPlugin.readers.size)
        androidOmapiPlugin.connectToSe(context)
        androidOmapiPlugin.onConnected() //Thanks to the object mockk we can simulate readers retrieval
        assertEquals(2, androidOmapiPlugin.readers.size)

    }

    private fun mockContext(): Context{
        val context= mockk<Context>()
        every { context.applicationContext } returns context
        return context
    }

    private fun mockReader(name: String, isPresent: Boolean): Reader{
        val reader= mockk<Reader>()
        every { reader.isSecureElementPresent } returns isPresent
        every { reader.name } returns name
        return reader
    }
}