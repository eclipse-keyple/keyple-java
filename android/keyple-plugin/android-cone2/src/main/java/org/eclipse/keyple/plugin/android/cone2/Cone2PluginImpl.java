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
package org.eclipse.keyple.plugin.android.cone2;

import android.content.Context;

import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractPlugin;
import org.eclipse.keyple.core.seproxy.plugin.AbstractThreadedObservablePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.coppernic.sdk.ask.Reader;
import fr.coppernic.sdk.power.impl.cone.ConePeripheral;
import fr.coppernic.sdk.utils.core.CpcResult;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Enables Keyple to communicate with the the C-One² ASK RFID reader.
 */

final class Cone2PluginImpl extends AbstractThreadedObservablePlugin implements Cone2Plugin {
    private static final Logger LOG = LoggerFactory.getLogger(Cone2PluginImpl.class);

    private final Map<String, String> parameters = new HashMap<String, String>();// not in use in this
    // plugin
    private AtomicBoolean isReaderPoweredOn = new AtomicBoolean(false);

    Cone2PluginImpl() {
        super(PLUGIN_NAME);
    }

    @Override
    public Map<String, String> getParameters() {
        LOG.warn("Android C-One² Plugin does not support parameters, see AndroidCone2Reader instead");
        return parameters;
    }

    @Override
    public void setParameter(String key, String value) {
        LOG.warn("Android C-One² Plugin does not support parameters, see AndroidCone2Reader instead");
        parameters.put(key, value);
    }


    /**
     * For an Android C-One² device, the Android C-One² Plugin manages only one
     * {@link Cone2ContactlessReaderImpl} and 2 {@link Cone2ContactReaderImpl} .
     * 
     * @return SortedSet<ProxyReader> : contains only one element, the
     *         singleton {@link Cone2ContactlessReaderImpl}
     */
    @Override
    protected SortedSet<SeReader> initNativeReaders() {
        if (isReaderPoweredOn != null && isReaderPoweredOn.get()) {
            LOG.debug("InitNativeReader() add the unique instance of AndroidCone2Reader");
            readers = new TreeSet<SeReader>();
            Cone2ContactlessReaderImpl contactlessReader = new Cone2ContactlessReaderImpl();
            readers.add(contactlessReader);
            readersNames.add(contactlessReader.getName());
            Cone2ContactReaderImpl sam1 = new Cone2ContactReaderImpl();
            sam1.setParameter(Cone2ContactReader.CONTACT_INTERFACE_ID
                    , Cone2ContactReader.CONTACT_INTERFACE_ID_SAM_1);
            readers.add(sam1);
            readersNames.add(sam1.getName());
            Cone2ContactReaderImpl sam2 = new Cone2ContactReaderImpl();
            sam2.setParameter(Cone2ContactReader.CONTACT_INTERFACE_ID,
                    Cone2ContactReader.CONTACT_INTERFACE_ID_SAM_2);
            readers.add(sam2);
            readersNames.add(sam2.getName());
            return readers;
        } else {
            return null;
        }
    }

    /**
     * Returns the C-One² Reader whatever is the provided name
     *
     * @param name : name of the reader to retrieve
     * @return instance of @{@link Cone2ContactlessReaderImpl}
     */
    @Override
    protected SeReader fetchNativeReader(String name) throws KeypleReaderException {
        // Returns the current reader if it is already listed
        for (SeReader reader : readers) {
            if (reader.getName().equals(name)) {
                return reader;
            }
        }

        throw new KeypleReaderException("Reader " + name + " not found!");
    }

    SortedSet<String> readersNames = new TreeSet<String>();

    @Override
    public void power(final Context context, final boolean on) {
        // Stops waiting for card when reader is powered off
        if(!on) {
            for (SeReader reader:readers) {
                if (reader.getName().compareTo(Cone2ContactlessReader.READER_NAME) == 0) {
                    ((Cone2ContactlessReaderImpl)reader).stopWaitForCard();
                }
            }
        }

        ConePeripheral.RFID_ASK_UCM108_GPIO.getDescriptor().power(context, on)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleObserver<CpcResult.RESULT>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(CpcResult.RESULT result) {
                if (readers != null) {
                    for (SeReader reader : readers) {
                        readersNames.add(reader.getName());
                    }
                }

                if (on) {
                    isReaderPoweredOn.set(true);
                    Cone2AskReader.getInstance(context, new Cone2AskReader.ReaderListener() {
                        @Override
                        public void onInstanceAvailable(Reader reader) {
                            initNativeReaders();
                        }

                        @Override
                        public void onError(int error) {

                        }
                    });
                } else {
                    isReaderPoweredOn.set(false);
                    Cone2AskReader.clearInstance();
                    notifyObservers(new PluginEvent(PLUGIN_NAME, readersNames, PluginEvent.EventType.READER_DISCONNECTED));
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

    @Override
    protected SortedSet<String> fetchNativeReadersNames() throws KeypleReaderException {
        return readersNames;
    }
}
