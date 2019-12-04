package org.eclipse.keyple.plugin.android.cone2;

import org.eclipse.keyple.core.seproxy.AbstractPluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;

/**
 * This class is a factory for Cone2Plugin
 */
public class Cone2Factory extends AbstractPluginFactory {

    public Cone2Factory() {

    }

    @Override
    public String getPluginName() {
        return Cone2PluginImpl.PLUGIN_NAME;
    }

    @Override
    protected ReaderPlugin getPluginInstance() {
        return new Cone2PluginImpl();
    }
}
