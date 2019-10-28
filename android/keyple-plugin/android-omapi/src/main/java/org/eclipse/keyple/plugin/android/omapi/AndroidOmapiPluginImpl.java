/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.android.omapi;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractPlugin;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import android.util.Log;

/**
 * Loads and configures {@link AndroidOmapiReaderImpl} for each SE Reader in the platform
 */
final class AndroidOmapiPluginImpl extends AbstractPlugin implements AndroidOmapiPlugin, SEService.CallBack {

    private static final String TAG = AndroidOmapiPluginImpl.class.getSimpleName();

    private SEService seService;
    private ISeServiceFactory seServiceFactory;


    // singleton methods
    private static AndroidOmapiPluginImpl uniqueInstance = null;

    static ISeServiceFactory getSeServiceFactory() {
        return new SeServiceFactoryImpl();
    };


    /**
     * Initialize plugin by connecting to {@link SEService} ; Make sure to instantiate Android Omapi
     * Plugin from a Android Context Application
     */
    AndroidOmapiPluginImpl() {
        super(PLUGIN_NAME);
        seServiceFactory = AndroidOmapiPluginImpl.getSeServiceFactory();
        seService = seServiceFactory.connectToSe(this);
        Log.i(TAG, "OMAPI SEService version: " + seService.getVersion());
    }


    public static AndroidOmapiPluginImpl getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new AndroidOmapiPluginImpl();
        }
        return uniqueInstance;


    }


    @Override
    protected SortedSet<SeReader> initNativeReaders() {

        SortedSet<SeReader> readers = new TreeSet<SeReader>();

        if (seService != null && seService.isConnected()) {
            Reader[] omapiReaders = seService.getReaders();

            // no readers found in the environment, don't return any readers for keyple
            if (omapiReaders == null) {
                Log.w(TAG, "No readers found");
                return readers;// empty list
            }

            // Build a keyple reader for each readers found by the OMAPI
            for (Reader omapiReader : omapiReaders) {
                Log.d(TAG, "Reader available name : " + omapiReader.getName());
                Log.d(TAG,
                        "Reader available isSePresent : " + omapiReader.isSecureElementPresent());

                // http://seek-for-android.github.io/javadoc/V4.0.0/org/simalliance/openmobileapi/Reader.html
                SeReader seReader =
                        new AndroidOmapiReaderImpl(PLUGIN_NAME, omapiReader, omapiReader.getName());
                readers.add(seReader);
            }

            return readers;

        } else {
            Log.w(TAG, "OMAPI SeService is not connected yet");
            return readers;// empty list
        }

    }

    /**
     * Warning. Do not call this method directly.
     *
     * Invoked by Open Mobile {@link SEService} when connected
     * Instanciates {@link AndroidOmapiReaderImpl} for each SE Reader detected in the platform
     * 
     * @param seService : connected omapi service
     */
    @Override
    public void serviceConnected(SEService seService) {

        Log.i(TAG, "Retrieve available readers...");

        // init readers
        readers = initNativeReaders();
    }

    private Map<String, String> parameters = new HashMap<String, String>();// not in use in this
    // plugin

    @Override
    public Map<String, String> getParameters() {
        Log.w(TAG, "Android OMAPI Plugin does not support parameters, see OMAPINfcReader instead");
        return parameters;
    }

    @Override
    public void setParameter(String key, String value) {
        Log.w(TAG, "Android OMAPI  Plugin does not support parameters, see OMAPINfcReader instead");
        parameters.put(key, value);
    }


}
