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
package org.eclipse.keyple.plugin.android.nfc;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractPlugin;
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
 *
 */

final class AndroidNfcPluginImpl extends AbstractPlugin implements AndroidNfcPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidNfcPluginImpl.class);

    private final static AndroidNfcPluginImpl uniqueInstance = new AndroidNfcPluginImpl();

    private final Map<String, String> parameters = new HashMap<String, String>();// not in use in
                                                                                 // this

    // plugin

    private AndroidNfcPluginImpl() {
        super(PLUGIN_NAME);
    }

    public static AndroidNfcPluginImpl getInstance() {
        return uniqueInstance;
    }

    @Override
    public Map<String, String> getParameters() {
        LOG.warn("Android NFC Plugin does not support parameters, see AndroidNfcReaderImpl instead");
        return parameters;
    }

    @Override
    public void setParameter(String key, String value) {
        LOG.warn("Android NFC Plugin does not support parameters, see AndroidNfcReaderImpl instead");
        parameters.put(key, value);
    }


    /**
     * For an Android NFC device, the Android NFC Plugin manages only one @{@link AndroidNfcReaderImpl}.
     * 
     * @return SortedSet<ProxyReader> : contains only one element, the
     *         singleton @{@link AndroidNfcReaderImpl}
     */
    @Override
    protected SortedSet<SeReader> initNativeReaders() {
        LOG.debug("InitNativeReader() add the unique instance of AndroidNfcReaderImpl");
        // return the only one reader in a list
        SortedSet<SeReader> readers = new TreeSet<SeReader>();
        readers.add(AndroidNfcReaderImpl.getInstance());
        return readers;
    }


    /**
     * Return the AndroidNfcReaderImpl whatever is the provided name
     * 
     * @param name : name of the reader to retrieve
     * @return instance of @{@link AndroidNfcReaderImpl}
     */
    @Override
    protected SeReader fetchNativeReader(String name) {
        return readers.first();
    }
}
