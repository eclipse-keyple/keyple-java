/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.android.omapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReadersPlugin;
import org.eclipse.keyple.seproxy.event.AbstractObservablePlugin;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import android.util.Log;


public class AndroidOmapiPlugin extends AbstractObservablePlugin implements SEService.CallBack {

    private static final String TAG = AndroidOmapiPlugin.class.getSimpleName();

    private Map<String, ProxyReader> proxyReaders;

    private Map<String, String> parameters = new HashMap<String, String>();// not in use in this
    // plugin


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
        return "OMAPINFCPlugin";
    }

    @Override
    public Map<String, String> getParameters() {
        Log.w(TAG, "Android OMAPI Plugin does not support parameters, see OMAPINfcReader instead");
        return parameters;
    }

    @Override
    public void setParameter(String key, String value) throws IOException {
        Log.w(TAG, "Android OMAPI  Plugin does not support parameters, see OMAPINfcReader instead");
        parameters.put(key, value);
    }


    @Override
    public SortedSet<? extends ProxyReader> getReaders() throws IOReaderException {
        return new TreeSet<ProxyReader>(proxyReaders.values());
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
            Log.d(TAG, "Reader available isSePresent : " + omapiReader.isSecureElementPresent());

            // http://seek-for-android.github.io/javadoc/V4.0.0/org/simalliance/openmobileapi/Reader.html
            ProxyReader seReader = new AndroidOmapiReader(omapiReader);
            proxyReaders.put(omapiReader.getName(), seReader);

        }

    }

}
