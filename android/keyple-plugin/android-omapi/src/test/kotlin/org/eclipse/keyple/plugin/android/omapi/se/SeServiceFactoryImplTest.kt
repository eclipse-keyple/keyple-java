package org.eclipse.keyple.plugin.android.omapi.se

import android.content.Context
import android.se.omapi.SEService
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SeServiceFactoryImplTest {

    lateinit var seServiceFactory: SeServiceFactoryImpl

    @Before
    fun setUp(){
        val context= mockk<Context>()
        seServiceFactory= SeServiceFactoryImpl(context)
    }

    @Test
    fun connectToSe(){
        val seService = seServiceFactory.connectToSe(SEService.OnConnectedListener {})
        Assert.assertNotNull(seService)
    }
}