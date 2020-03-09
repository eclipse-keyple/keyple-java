package org.eclipse.keyple.plugin.android.cone2;

import android.content.Context;

import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginException;

public interface Cone2Plugin extends ObservablePlugin {
    String PLUGIN_NAME = "Cone2Plugin";

    /**
     * Powers on/off reader
     * @param on true on, false off
     */
    void power(Context context, boolean on) throws KeyplePluginException;
}
