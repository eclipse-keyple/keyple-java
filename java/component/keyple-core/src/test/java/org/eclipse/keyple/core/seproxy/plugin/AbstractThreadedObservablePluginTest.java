package org.eclipse.keyple.core.seproxy.plugin;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.plugin.mock.MockAbstractThreadedPlugin;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractThreadedObservablePluginTest  extends CoreBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractThreadedObservablePluginTest.class);


    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }


    @Test
    public void addObserver() throws Throwable {
        MockAbstractThreadedPlugin plugin = new MockAbstractThreadedPlugin("addObserverTest");

        //add observer
        plugin.addObserver(getOneObserver());

        Assert.assertEquals(1, plugin.countObservers());
        //test if thread is activated
        Assert.assertTrue(plugin.isMonitoring());


        //shutdown thread
        plugin.finalize();
    }

    @Test
    public void removeObserver() throws Throwable {
        MockAbstractThreadedPlugin plugin = new MockAbstractThreadedPlugin("addObserverTest");

        ObservablePlugin.PluginObserver obs = getOneObserver();

        //add observer
        plugin.addObserver(obs);
        plugin.removeObserver(obs);

        Assert.assertEquals(0, plugin.countObservers());
        //test if thread is activated
        Assert.assertFalse(plugin.isMonitoring());

        plugin.finalize();

    }







    /*
     * Helpers
     */
    ObservablePlugin.PluginObserver getOneObserver(){
        return new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {

            }
        };
    }

}
