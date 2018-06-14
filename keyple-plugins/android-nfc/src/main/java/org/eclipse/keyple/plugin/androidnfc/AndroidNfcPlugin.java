/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.androidnfc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReadersPlugin;
import android.util.Log;


/**
 * Enables Keyple to communicate with the the Android device embedded NFC reader. In the Android
 * platform, NFC reader must be link to an application activity.
 *
 *
 * To activate NFC Keyple capabilities, add {@link AndroidNfcFragment} to the application activity.
 * getFragmentManager().beginTransaction().add(AndroidNfcFragment.newInstance(),
 * "myFragmentId").commit();
 *
 * Then set the Activity as an observer of the plugin as any Keyple plugin :
 *
 * SeProxyService seProxyService = SeProxyService.getInstance(); List<ReadersPlugin> plugins = new
 * ArrayList<ReadersPlugin>(); plugins.add(AndroidNfcPlugin.getInstance());
 * seProxyService.setPlugins(plugins);
 *
 * ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);
 * ((AbstractObservableReader) reader).addObserver(this);
 *
 *
 *
 */

public class AndroidNfcPlugin implements ReadersPlugin {

    private static final String TAG = AndroidNfcPlugin.class.getSimpleName();

    private final static AndroidNfcPlugin uniqueInstance = new AndroidNfcPlugin();

    private ProxyReader reader;

    private AndroidNfcPlugin() {

        if (this.reader == null) {
            Log.i(TAG, "Instanciate singleton NFC Plugin");
            this.reader = AndroidNfcReader.getInstance();
        }

    }

    public static AndroidNfcPlugin getInstance() {
        return uniqueInstance;
    }


    @Override
    public String getName() {
        return "AndroidNFCPlugin";
    }

    /**
     * For an Android NFC device, the Android NFC Plugin manages only one @{@link AndroidNfcReader}.
     * 
     * @return List<ProxyReader> : contains only one element, the
     *         singleton @{@link AndroidNfcReader}
     */
    @Override
    public List<ProxyReader> getReaders() {
        // return the only one reader in a list
        List<ProxyReader> readers = new ArrayList<ProxyReader>();
        readers.add(reader);
        return readers;
    }

    @Override
    public final Map<String, String> getParameters() {
        return null;
    }

    @Override
    public final void setParameter(String key, String value) throws IOException {

    }

    public final void setParameters(Map<String, String> parameters) throws IOException {
        // empty in the Android case
    }
}
