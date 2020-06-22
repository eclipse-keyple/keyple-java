package org.eclipse.keyple.plugin.remotese.nativese.impl;

import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class AbstractNativeSeServiceTest {


    private static final Logger logger = LoggerFactory.getLogger(AbstractNativeSeServiceTest.class);


    /**
     * Find local reader among plugin no plugin
     * @throws Exception
     */
    @Test(expected = KeypleReaderNotFoundException.class)
    public void testFindLocalReader_notFound() throws Exception {
        AbstractNativeSeService abstractNativeSeService = new AbstractNativeSeService() {
            @Override
            protected void onMessage(KeypleMessageDto msg) {

            }
        };

        abstractNativeSeService.findLocalReader("test");
        //should throw exception
    }


}
