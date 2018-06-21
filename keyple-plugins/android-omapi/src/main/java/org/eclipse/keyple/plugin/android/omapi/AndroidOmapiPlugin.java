/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.android.omapi;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.event.AbstractObservablePlugin;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import android.app.Application;
import android.util.Log;

/**
 * Loads and configures {@link AndroidOmapiReader} for each SE Reader in the platform TODO : filters
 * readers to load by parameters with a regex
 */
public class AndroidOmapiPlugin extends AbstractObservablePlugin implements SEService.CallBack {

    private static final String TAG = AndroidOmapiPlugin.class.getSimpleName();

    private Map<String, ProxyReader> proxyReaders = new HashMap<String, ProxyReader>();

    private SEService seService;


    // singleton methods
    private static AndroidOmapiPlugin uniqueInstance = new AndroidOmapiPlugin();

    /**
     * Initialize plugin by connecting to {@link SEService} Application Context is retrieved
     * automatically by a reflection invocation to method
     * {@linkandroid.app.ActivityThread#currentApplication} Make sure to instantiate Android Omapi
     * Plugin from a Android Context Application
     */
    private AndroidOmapiPlugin() {
        try {
            Log.i(TAG, "Retrieving Application Context with reflection android.app.AppGlobals");

            Application app = (Application) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null, (Object[]) null);


            if (seService == null || !seService.isConnected()) {
                seService = new SEService(app, this);
                Log.i(TAG, "Connected to SeService " + seService.getVersion());

            } else {
                Log.w(TAG, "seService was already connected");
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static AndroidOmapiPlugin getInstance() {
        return uniqueInstance;
    }


    @Override
    public String getName() {
        return "OMAPINFCPlugin";
    }


    /**
     * Returns all {@link AndroidOmapiReader} readers loaded by the plugin
     * 
     * @return {@link AndroidOmapiReader} readers loaded by the plugin
     * @throws IOReaderException
     */
    @Override
    public SortedSet<? extends ProxyReader> getReaders() throws IOReaderException {
        return new TreeSet<ProxyReader>(proxyReaders.values());
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

    private Map<String, String> parameters = new HashMap<String, String>();// not in use in this
    // plugin

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


}
