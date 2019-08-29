package org.eclipse.keyple.plugin.android.nfc;

import org.eclipse.keyple.core.seproxy.PluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;

public final class AndroidNfcPluginFactory extends PluginFactory {
    @Override
    public String getPluginName() {
        return AndroidNfcPlugin.PLUGIN_NAME;
    }

    @Override
    protected ReaderPlugin getPluginInstance() {
        return AndroidNfcPluginImpl.getInstance();
    }

}
