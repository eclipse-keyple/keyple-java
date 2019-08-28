package org.eclipse.keyple.plugin.pcsc;

import org.eclipse.keyple.core.seproxy.PluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;

public class PcscPluginFactory extends PluginFactory {

    @Override
    protected String getPluginName() {
        return "PcscPlugin";
    }

    @Override
    protected ReaderPlugin getPluginInstance() {
        return PcscPlugin.getInstance();
    }
}
