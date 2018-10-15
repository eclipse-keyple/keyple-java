/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.plugin.android.omapi;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.eclipse.keyple.seproxy.plugin.AbstractStaticPlugin;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import android.util.Log;

/**
 * Loads and configures {@link AndroidOmapiReader} for each SE Reader in the platform TODO : filters
 * readers to load by parameters with a regex
 */
public class AndroidOmapiPlugin extends AbstractStaticPlugin implements SEService.CallBack {

    private static final String TAG = AndroidOmapiPlugin.class.getSimpleName();
    public static final String PLUGIN_NAME = "AndroidOmapiPlugin";

    private SEService seService;
    private ISeServiceFactory seServiceFactory;


    // singleton methods
    private static AndroidOmapiPlugin uniqueInstance = null;

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
    }


    public static AndroidOmapiPlugin getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new AndroidOmapiPlugin();
        }
        return uniqueInstance;


    }


    /**
     * Returns all {@link AndroidOmapiReader} readers loaded by the plugin
     * 
     * @return {@link AndroidOmapiReader} readers loaded by the plugin
     * @throws KeypleReaderException
     */
    /*
     * @Override public SortedSet<? extends ProxyReader> getReaders() throws KeypleReaderException {
     * return new TreeSet<ProxyReader>(proxyReaders.values()); }
     */

    @Override
    protected SortedSet<AbstractObservableReader> getNativeReaders() {

        SortedSet<AbstractObservableReader> readers = new TreeSet<AbstractObservableReader>();

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

    @Override
    protected AbstractObservableReader getNativeReader(String name) throws KeypleReaderException {
        for (AbstractObservableReader aReader : readers) {
            if (aReader.getName().equals(name)) {
                return aReader;
            }
        }
        return null;

    }

    /**
     * Do not call this method directly. Invoked by Open Mobile {@link SEService} when connected
     * Instanciates {@link AndroidOmapiReader} for each SE Reader detected in the platform
     * 
     * @param seService
     */
    @Override
    public void serviceConnected(SEService seService) {

        Log.i(TAG, "Retrieve available readers...");

        // init readers
        readers = getNativeReaders();
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
