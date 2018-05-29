/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.android.omapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReadersPlugin;
import org.eclipse.keyple.seproxy.exceptions.IOReaderException;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import android.util.Log;


public class AndroidOmapiPlugin implements ReadersPlugin, SEService.CallBack {

    private static final String TAG = AndroidOmapiPlugin.class.getSimpleName();

    private Map<String, ProxyReader> proxyReaders;

    // singleton methods
    private static AndroidOmapiPlugin uniqueInstance = new AndroidOmapiPlugin();

    private AndroidOmapiPlugin() {
        // empty constructor
        proxyReaders = new HashMap<String, ProxyReader>();
    }

    public static AndroidOmapiPlugin getInstance() {
        return uniqueInstance;
    }


    @Override
    public String getName() {
        return TAG;
    }


    @Override
    public List<? extends ProxyReader> getReaders() throws IOReaderException {
        return new ArrayList<ProxyReader>(proxyReaders.values());
    }

    /**
     * /called automatically by omapi platform/ When SE Service is connected, retrieve available
     * readers
     * 
     * @param seService
     */
    @Override
    public void serviceConnected(SEService seService) {
        Log.i(TAG, "seviceConnected()");


        Log.i(TAG, "Retrieve available readers...");
        Reader[] omapiReaders = seService.getReaders();
        if (omapiReaders.length < 1) {

            Log.w(TAG, "No readers found");
            return;
        }


        for (Reader omapiReader : omapiReaders) {
            Log.d(TAG, "Reader available name : " + omapiReader.getName());
            Log.d(TAG, "Reader available isSEPresent : " + omapiReader.isSecureElementPresent());

            // http://seek-for-android.github.io/javadoc/V4.0.0/org/simalliance/openmobileapi/Reader.html
            ProxyReader seReader = new AndroidOmapiReader(omapiReader);
            proxyReaders.put(omapiReader.getName(), seReader);

        }

    }

}
