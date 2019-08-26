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
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractStaticPlugin;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import android.util.Log;

/**
 * Loads and configures {@link AndroidOmapiReader} for each SE Reader in the platform TODO : filters
 * readers to load by parameters with a regex
 */
final class AndroidOmapiPlugin extends AbstractStaticPlugin implements SEService.CallBack {

    private static final String TAG = AndroidOmapiPlugin.class.getSimpleName();
    public static final String PLUGIN_NAME = "AndroidOmapiPlugin";

    private SEService seService;
    private ISeServiceFactory seServiceFactory;

    static ISeServiceFactory getSeServiceFactory() {
        return new SeServiceFactoryImpl();
    };


    /**
     * Initialize plugin by connecting to {@link SEService} ; Make sure to instantiate Android Omapi
     * Plugin from a Android Context Application
     */
    AndroidOmapiPlugin() {
        super(PLUGIN_NAME);
        seServiceFactory = AndroidOmapiPlugin.getSeServiceFactory();
        seService = seServiceFactory.connectToSe(this);
        Log.i(TAG, "OMAPI SEService version: " + seService.getVersion());
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
                AbstractObservableReader seReader =
                        new AndroidOmapiReader(PLUGIN_NAME, omapiReader, omapiReader.getName());
                readers.add(seReader);
            }

            return readers;

        } else {
            Log.w(TAG, "OMAPI SeService is not connected yet");
            return readers;// empty list
        }

    }

    /**
     * Fetch connected native reader (from third party library) by its name Returns the current
     * {@link org.eclipse.keyple.core.seproxy.plugin.AbstractObservableReader} if it is already listed.
     *
     * @param name reader name to be fetched
     * @return the list of AbstractObservableReader objects.
     * @throws KeypleReaderNotFoundException if reader is not found
     */
    @Override
    protected AbstractObservableReader fetchNativeReader(String name)
            throws KeypleReaderNotFoundException {
        return (AbstractObservableReader) this.getReader(name);
    }

    /**
     * Warning. Do not call this method directly.
     *
     * Invoked by Open Mobile {@link SEService} when connected
     * Instanciates {@link AndroidOmapiReader} for each SE Reader detected in the platform
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
