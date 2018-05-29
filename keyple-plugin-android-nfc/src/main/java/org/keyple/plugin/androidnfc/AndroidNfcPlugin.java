/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.androidnfc;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReadersPlugin;
import android.util.Log;


/**
 * Readers Plugin for Android platform based on the NFC Adapter
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
}
