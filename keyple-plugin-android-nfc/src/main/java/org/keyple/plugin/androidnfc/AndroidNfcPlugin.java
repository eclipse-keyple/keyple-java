/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.androidnfc;

import java.util.ArrayList;
import java.util.List;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.ReadersPlugin;
import org.keyple.seproxy.exceptions.IOReaderException;


/**
 * Readers Plugin for Android platform based on the NFC Adapter
 */
public class AndroidNfcPlugin implements ReadersPlugin {

    private static final String TAG = AndroidNfcPlugin.class.getSimpleName();

    private static AndroidNfcPlugin uniqueInstance = new AndroidNfcPlugin();

    private ProxyReader reader;

    private AndroidNfcPlugin() {

        if (this.reader == null) {
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
     * @throws IOReaderException
     */
    @Override
    public List<ProxyReader> getReaders() throws IOReaderException {
        List<ProxyReader> readers = new ArrayList<ProxyReader>();
        readers.add(reader);
        return readers;

    }
}
