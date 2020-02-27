package org.eclipse.keyple.plugin.android.nfc

import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.HashMap

class AndroidNfcPluginImplTest {

    private lateinit var plugin: AndroidNfcPluginImpl

    // init before each test
    @Before
    @Throws(IOException::class)
    fun setUp() {
        // get unique instance
        plugin = AndroidNfcPluginImpl
    }


    /*
     * TEST PUBLIC METHODS
     */


    @Test
    @Throws(IOException::class)
    fun getInstance() {
        Assert.assertNotNull(plugin)
    }

    @Test
    fun pluginName(){
        Assert.assertEquals("AndroidNfcPlugin", AndroidNfcPlugin.PLUGIN_NAME)
    }

    @Test
    @Throws(KeypleBaseException::class)
    fun setParameters() {

        val parameters = HashMap<String, String>()
        parameters["key1"] = "value1"
        plugin.parameters = parameters
        Assert.assertTrue(plugin.parameters.isNotEmpty())
        Assert.assertEquals("value1", plugin.parameters["key1"])

    }

    @Test
    @Throws(IOException::class)
    fun getParameters() {
        Assert.assertNotNull(plugin.parameters)
    }


    @Test
    @Throws(IOException::class)
    fun setParameter() {
        plugin.setParameter("key2", "value2")
        Assert.assertTrue(plugin.parameters.isNotEmpty())
        Assert.assertEquals("value2", plugin.parameters["key2"])
    }

    @Test
    @Throws(KeypleReaderException::class)
    fun getReaders() {
        Assert.assertEquals(1, plugin.readers.size)
        Assert.assertTrue(plugin.readers.first() is AndroidNfcReaderImpl)
    }

    @Test
    @Throws(Exception::class)
    fun getName() {
        Assert.assertEquals(AndroidNfcPluginImpl.name, plugin.name)
    }

    /*
     * TEST INTERNAL METHODS
     */

    @Test
    @Throws(Exception::class)
    fun getNativeReader() {
        Assert.assertTrue(plugin.getReader(AndroidNfcReaderImpl.name) is AndroidNfcReaderImpl)
    }

    @Test
    @Throws(Exception::class)
    fun getNativeReaders() {
        Assert.assertEquals(1, plugin.readers.size)
        Assert.assertTrue(plugin.readers.first() is AndroidNfcReaderImpl)
    }


}