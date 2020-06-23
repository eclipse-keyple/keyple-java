package org.eclipse.keyple.plugin.remotese.nativese.impl;

import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class NativeSeClientServiceFactoryTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNativeSeServiceTest.class);

    @Test
    public void buildSync() throws Exception {
        //init
        KeypleClientSync syncClient = Mockito.mock(KeypleClientSync.class);
        //test
        NativeSeClientService service = new NativeSeClientServiceFactory()
                .builder()
                .withSyncNode(syncClient)
                .withReaderObservation()
                .getService();

        //assert
        Assert.assertNotNull(service);

    }

    @Test
    public void buildASync() throws Exception {
        //init
        KeypleClientAsync asyncClient = Mockito.mock(KeypleClientAsync.class);
        //test
        NativeSeClientService service = new NativeSeClientServiceFactory()
                .builder()
                .withAsyncNode(asyncClient)
                .withReaderObservation()
                .getService();

        //assert
        Assert.assertNotNull(service);

    }

}
