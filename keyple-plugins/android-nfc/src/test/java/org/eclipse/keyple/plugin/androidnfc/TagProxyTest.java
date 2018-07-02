package org.eclipse.keyple.plugin.androidnfc;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.util.Observable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class TagProxyTest {

    private static final ILogger logger = SLoggerFactory.getLogger(TagProxyTest.class);


    @Before
    public void SetUp() throws IOReaderException {

    }

    /**
     *
     * @throws IOReaderException
     */
    @Test
    public void GetATR() throws IOReaderException {


    }


}
