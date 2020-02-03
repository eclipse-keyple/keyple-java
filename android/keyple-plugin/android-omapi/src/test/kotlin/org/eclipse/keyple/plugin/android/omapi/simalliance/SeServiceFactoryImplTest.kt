package org.eclipse.keyple.plugin.android.omapi.simalliance

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.simalliance.openmobileapi.SEService

class SeServiceFactoryImplTest {

    private lateinit var seServiceFactory: SeServiceFactoryImpl

    @Before
    fun setUp() {
        val context = mockk<Context>()
        every { context.bindService(any(), any(), any()) } returns false
        seServiceFactory = SeServiceFactoryImpl(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun connectToSe() {
        val seService = seServiceFactory.connectToSe(SEService.CallBack { })
        Assert.assertNotNull(seService)
    }
}
