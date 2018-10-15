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

package org.eclipse.keyple.plugin.android.nfc;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.eclipse.keyple.seproxy.plugin.AbstractStaticPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



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
 * SeProxyService seProxyService = SeProxyService.getInstance(); List<ReaderPlugin> plugins = new
 * ArrayList<ReaderPlugin>(); plugins.add(AndroidNfcPlugin.getInstance());
 * seProxyService.setPlugins(plugins);
 *
 * ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);
 * ((AbstractObservableReader) reader).addObserver(this);
 *
 *
 *
 */

public class AndroidNfcPlugin extends AbstractStaticPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidNfcPlugin.class);

    private final static AndroidNfcPlugin uniqueInstance = new AndroidNfcPlugin();

    static final String PLUGIN_NAME = "AndroidNFCPlugin";

    private final Map<String, String> parameters = new HashMap<String, String>();// not in use in
                                                                                 // this

    // plugin

    private AndroidNfcPlugin() {
        super("AndroidNFCPlugin");
    }

    public static AndroidNfcPlugin getInstance() {
        return uniqueInstance;
    }

    @Override
    public Map<String, String> getParameters() {
        LOG.warn("Android NFC Plugin does not support parameters, see AndroidNfcReader instead");
        return parameters;
    }

    @Override
    public void setParameter(String key, String value) {
        LOG.warn("Android NFC Plugin does not support parameters, see AndroidNfcReader instead");
        parameters.put(key, value);
    }


    /**
     * For an Android NFC device, the Android NFC Plugin manages only one @{@link AndroidNfcReader}.
     * 
     * @return SortedSet<ProxyReader> : contains only one element, the
     *         singleton @{@link AndroidNfcReader}
     */
    @Override
    protected SortedSet<AbstractObservableReader> getNativeReaders() {
        // return the only one reader in a list
        SortedSet<AbstractObservableReader> readers =
                new ConcurrentSkipListSet<AbstractObservableReader>();
        readers.add(AndroidNfcReader.getInstance());
        return readers;
    }


    /**
     * Return the AndroidNfcReader whatever is the provided name
     * 
     * @param name : name of the reader to retrieve
     * @return instance of @{@link AndroidNfcReader}
     */
    protected AbstractObservableReader getNativeReader(String name) {
        return readers.first();
    }
}
