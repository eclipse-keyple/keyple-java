/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.transaction;


import java.util.*;
import java.util.regex.Pattern;
import org.eclipse.keyple.core.seproxy.*;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.keyple.calypso.transaction.SamResourceManagerFactory.MAX_BLOCKING_TIME;
import static org.eclipse.keyple.calypso.transaction.SamResourceManagerFactory.createSamResource;


/**
 * Management of SAM resources:
 * <p>
 * Provides methods fot the allocation/deallocation of SAM resources
 */
public class SamResourceManagerDefault implements SamResourceManager{
    private static final Logger logger = LoggerFactory.getLogger(SamResourceManagerDefault.class);

    private final List<SamResource> localSamResources = new ArrayList<SamResource>();
    final SamResourceManagerDefault.ReaderObserver readerObserver;// used with observable readers
    protected final ReaderPlugin samReaderPlugin;


    protected SamResourceManagerDefault(ReaderPlugin readerPlugin, String samReaderFilter)
            throws KeypleReaderException {
        this.samReaderPlugin = readerPlugin;

        readerObserver = new SamResourceManagerDefault.ReaderObserver();
        logger.info(
                "PLUGINNAME = {} initialize the localSamResources with the {} connected readers filtered by {}",
                samReaderPlugin.getName(), samReaderPlugin.getReaders().size(), samReaderFilter);

        SortedSet<? extends SeReader> samReaders = samReaderPlugin.getReaders();
        for (SeReader samReader : samReaders) {
            String readerName = samReader.getName();
            Pattern p = Pattern.compile(samReaderFilter);
            if (p.matcher(readerName).matches()) {
                logger.debug("Add reader: {}", readerName);
                initSamReader(samReader, readerObserver);
            } else {
                logger.debug("Reader not matching: {}", readerName);
            }
        }

        if (readerPlugin instanceof ObservablePlugin) {

            // add an observer to monitor reader and SAM insertions

            SamResourceManagerDefault.PluginObserver pluginObserver =
                    new SamResourceManagerDefault.PluginObserver(readerObserver, samReaderFilter);
            logger.info("Add observer PLUGINNAME = {}", samReaderPlugin.getName());
            ((ObservablePlugin) samReaderPlugin).addObserver(pluginObserver);
        }

    }

    /**
     * Remove a {@link SamResource}from the current SamResource list
     *
     * @param samReader the SAM reader of the resource to remove from the list.
     */
    protected void removeResource(SeReader samReader) {
        ListIterator<SamResource> iterator = localSamResources.listIterator();
        while (iterator.hasNext()) {
            SamResource currentSamResource = iterator.next();
            if (currentSamResource.getSeReader().equals(samReader)) {
                if (logger.isInfoEnabled()) {
                    logger.info(
                            "Freed SAM resource: READER = {}, SAM_REVISION = {}, SAM_SERIAL_NUMBER = {}",
                            samReader.getName(),
                            currentSamResource.getMatchingSe().getSamRevision(), ByteArrayUtil
                                    .toHex(currentSamResource.getMatchingSe().getSerialNumber()));
                }
                iterator.remove();
            }
        }
    }


    public SamResource allocateSamResource(SamResourceManagerFactory.AllocationMode allocationMode,
                                           SamIdentifier samIdentifier) throws KeypleReaderException {
        long maxBlockingDate = System.currentTimeMillis() + MAX_BLOCKING_TIME;
        boolean noSamResourceLogged = false;
        logger.debug("Allocating SAM reader channel...");
        while (true) {
            synchronized (localSamResources) {
                for (SamResource samResource : localSamResources) {
                    if (samResource.isSamResourceFree()) {
                        if (samResource.isSamMatching(samIdentifier)) {
                            samResource.setSamResourceStatus(SamResource.SamResourceStatus.BUSY);
                            logger.debug("Allocation succeeded. SAM resource created.");
                            return samResource;
                        }
                    }
                }
            }

            // loop until MAX_BLOCKING_TIME in blocking mode, only once in non-blocking mode
            if (allocationMode == SamResourceManagerFactory.AllocationMode.NON_BLOCKING) {
                logger.trace("No SAM resources available at the moment.");
                break;
            } else {
                if (!noSamResourceLogged) {
                    /* log once the first time */
                    logger.trace("No SAM resources available at the moment.");
                    noSamResourceLogged = true;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // set interrupt flag
                    logger.error("Interrupt exception in Thread.sleep.");
                }
                if (System.currentTimeMillis() >= maxBlockingDate) {
                    logger.error("The allocation process failed. Timeout {} sec exceeded .",
                            (MAX_BLOCKING_TIME / 100.0));
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Free a previously allocated SAM resource.
     *
     * @param samResource the SAM resource reference to free
     */
    public void freeSamResource(SamResource samResource) {
        synchronized (localSamResources) {
            logger.debug("Freeing local SAM resource.");
            samResource.setSamResourceStatus(SamResource.SamResourceStatus.FREE);
        }
    }

    /**
     * Plugin observer to handle SAM reader connection/disconnection.
     * <p>
     * Add or remove readers
     * <p>
     * Add a reader observer when an {@link ObservableReader} is connected.
     */
    class PluginObserver implements ObservablePlugin.PluginObserver {

        final ReaderObserver readerObserver;
        final String samReaderFilter;
        Pattern p;

        PluginObserver(ReaderObserver readerObserver, String samReaderFilter) {
            this.readerObserver = readerObserver;
            this.samReaderFilter = samReaderFilter;
        }

        /**
         * Handle {@link PluginEvent}
         *
         * @param event the plugin event
         */
        @Override
        public void update(PluginEvent event) {
            for (String readerName : event.getReaderNames()) {
                SeReader samReader = null;
                logger.info("PluginEvent: PLUGINNAME = {}, READERNAME = {}, EVENTTYPE = {}",
                        event.getPluginName(), readerName, event.getEventType());

                /* We retrieve the reader object from its name. */
                try {
                    samReader = SeProxyService.getInstance().getPlugin(event.getPluginName())
                            .getReader(readerName);
                } catch (KeyplePluginNotFoundException e) {
                    logger.error("Plugin not found {}", event.getPluginName());
                } catch (KeypleReaderNotFoundException e) {
                    logger.error("Reader not found {}", readerName);
                }
                switch (event.getEventType()) {
                    case READER_CONNECTED:
                        if (containsReader(localSamResources, readerName)) {
                            logger.trace(
                                    "Reader is already present in the local samResources -  READERNAME = {}",
                                    readerName);
                            // do nothing
                            return;
                        }

                        logger.trace("New reader! READERNAME = {}", samReader.getName());


                        /*
                         * We are informed here of a connection of a reader.
                         *
                         * We add an observer to this reader if possible.
                         */
                        p = Pattern.compile(samReaderFilter);
                        if (p.matcher(readerName).matches()) {
                            /* Enable logging */
                            initSamReader(samReader, readerObserver);
                        } else {
                            logger.debug("Reader not matching: {}", readerName);
                        }
                        break;
                    case READER_DISCONNECTED:
                        /*
                         * We are informed here of a disconnection of a reader.
                         *
                         * The reader object still exists but will be removed from the reader list
                         * right after. Thus, we can properly remove the observer attached to this
                         * reader before the list update.
                         */
                        p = Pattern.compile(samReaderFilter);
                        if (p.matcher(readerName).matches()) {

                            logger.info("Reader removed. READERNAME = {}", readerName);
                            if (samReader instanceof ObservableReader) {
                                if (readerObserver != null) {
                                    logger.info(
                                            "Remove observer and stop detection READERNAME = {}",
                                            readerName);
                                    ((ObservableReader) samReader).removeObserver(readerObserver);
                                    ((ObservableReader) samReader).stopSeDetection();
                                } else {
                                    removeResource(samReader);
                                    logger.info(
                                            "Unplugged reader READERNAME = {} wasn't observed. Resource removed.",
                                            readerName);
                                }
                            }
                        } else {
                            logger.debug("Reader not matching: {}", readerName);
                        }
                        break;
                    default:
                        logger.info("Unexpected reader event. EVENT = {}",
                                event.getEventType().getName());
                        break;
                }
            }
        }



    }

    /**
     * Reader observer to handle SAM insertion/withdraw
     */
    class ReaderObserver implements ObservableReader.ReaderObserver {

        ReaderObserver() {
            super();
        }

        /**
         * Handle {@link ReaderEvent}
         * <p>
         * Create {@link SamResource}
         *
         * @param event the reader event
         */
        @Override
        public void update(ReaderEvent event) {
            SeReader samReader = null;
            try {
                samReader = samReaderPlugin.getReader(event.getReaderName());
            } catch (KeypleReaderNotFoundException e) {
                e.printStackTrace();
            }
            synchronized (localSamResources) {
                switch (event.getEventType()) {
                    case SE_MATCHED:
                    case SE_INSERTED:
                        SamResource newSamResource = null;
                        try {
                            /*
                             * although the reader allocation is dynamic, the SAM resource type is
                             * STATIC
                             */
                            newSamResource = createSamResource(samReader);
                        } catch (KeypleReaderException e) {
                            logger.error("Reader failure while creating a SamResource from {}",
                                    samReader.getName());
                            e.printStackTrace();
                        }
                        /* failures are ignored */
                        if (newSamResource != null) {
                            if (logger.isInfoEnabled()) {
                                logger.info(
                                        "Created SAM resource: READER = {}, SAM_REVISION = {}, SAM_SERIAL_NUMBER = {}",
                                        event.getReaderName(),
                                        newSamResource.getMatchingSe().getSamRevision(),
                                        ByteArrayUtil.toHex(
                                                newSamResource.getMatchingSe().getSerialNumber()));
                            }
                            localSamResources.add(newSamResource);
                        }
                        break;
                    case SE_REMOVED:
                    case TIMEOUT_ERROR:
                        removeResource(samReader);
                        break;
                }
            }
        }
    }

    /*
     * Helper
     */
    static private boolean containsReader(List<SamResource> samResources, String readerName) {
        for (SamResource resource : samResources) {
            if (readerName.equals(resource.getSeReader().getName())) {
                return true;
            }
        }
        return false;
    }

    private void initSamReader(SeReader samReader, ReaderObserver readerObserver) {
        try {
            /* contactless SE works with T0 protocol */
            samReader.setParameter("protocol", "T0");

            /* Shared mode */
            samReader.setParameter("mode", "shared");
        } catch (KeypleBaseException e) {
            logger.error("Wrong parameter", e);
        }

        try {
            if (samReader.isSePresent()) {
                logger.debug("Create SAM resource: {}", samReader.getName());
                synchronized (localSamResources) {
                    localSamResources.add(createSamResource(samReader));
                }
            }
        } catch (KeypleIOReaderException e) {
            logger.error("Error in reader", e);
        } catch (KeypleReaderException e) {
            logger.error("Error in reader", e);
        }

        if (samReader instanceof ObservableReader && readerObserver != null) {
            logger.info("Add observer and start detection READERNAME = {}", samReader.getName());
            ((ObservableReader) samReader).addObserver(readerObserver);
            ((ObservableReader) samReader).startSeDetection(ObservableReader.PollingMode.REPEATING);
        } else {
            logger.info("Sam Reader is not an ObservableReader = {}", samReader.getName());
        }
    }
}
