package org.keyple.plugin.stub;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StubPluginTest {

    StubPlugin stubPlugin;

    @Before
    public void setUp() throws IOReaderException {
    stubPlugin = StubPlugin.getInstance();

    }

    @Test
    public void testGetReaders() throws IOReaderException {
        assert(stubPlugin.getReaders().size() == 1);
    }

    @Test
    public void testGetName() throws IOReaderException {
        assert(stubPlugin.getName() != null);
    }

}
