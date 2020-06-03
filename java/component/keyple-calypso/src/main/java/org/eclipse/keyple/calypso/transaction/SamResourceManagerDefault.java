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
import org.eclipse.keyple.calypso.exception.CalypsoNoSamResourceAvailableException;
import org.eclipse.keyple.core.seproxy.*;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Sam Resource Manager working a {@link ReaderPlugin} (either Stub or Pcsc) It is
 * meant to work with a Keyple Pcsc Plugin or a Keyple Stub Plugin.
 */
public class SamResourceManagerDefault extends SamResourceManager {
    private static final Logger logger = LoggerFactory.getLogger(SamResourceManagerDefault.class);

    private final List<SamResource> localSamResources = new ArrayList<SamResource>();
    final SamResourceManagerDefault.ReaderObserver readerObserver;// only used with observable
                                                                  // readers
    protected final ReaderPlugin samReaderPlugin;
    /* the maximum time (in milliseconds) during which the BLOCKING mode will wait */
    private final int maxBlockingTime;
    /*
     * the sleep time between two tries (in milliseconds) during which the BLOCKING mode will wait
     */
    private final int sleepTime;

    /**
     * Protected constructor, use the {@link SamResourceManagerFactory}
     *
     * @param readerPlugin the plugin through which SAM readers are accessible
     * @param samReaderFilter the regular expression defining how to identify SAM readers among
     *        others.
     * @param maxBlockingTime the maximum duration for which the allocateSamResource method will
     *        attempt to allocate a new reader by retrying (in milliseconds)
     * @param sleepTime the duration to wait between two retries
     * @throws KeypleReaderException thrown if an error occurs while getting the readers list.
     */
    protected SamResourceManagerDefault(ReaderPlugin readerPlugin, String samReaderFilter,
            int maxBlockingTime, int sleepTime) throws KeypleReaderException {
        /*
         * Assign parameters
         */
        if (sleepTime < 1) {
            throw new IllegalArgumentException("Sleep time must be greater than 0");
        }
        if (maxBlockingTime < 1) {
            throw new IllegalArgumentException("Max Blocking Time must be greater than 0");
        }
        this.sleepTime = sleepTime;
        this.maxBlockingTime = maxBlockingTime;
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
                logger.trace("Add reader: {}", readerName);
                try {
                    initSamReader(samReader, readerObserver);
                } catch (KeypleReaderException e) {
                    logger.error("could not init samReader {}", samReader.getName(), e);
                }
            } else {
                logger.trace("Reader not matching: {}", readerName);
            }
        }

        if (readerPlugin instanceof ObservablePlugin) {

            // add an observer to monitor reader and SAM insertions

            SamResourceManagerDefault.PluginObserver pluginObserver =
                    new SamResourceManagerDefault.PluginObserver(readerObserver, samReaderFilter);
            logger.trace("Add observer PLUGINNAME = {}", samReaderPlugin.getName());
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
                    logger.trace(
                            "Freed SAM resource: READER = {}, SAM_REVISION = {}, SAM_SERIAL_NUMBER = {}",
                            samReader.getName(),
                            currentSamResource.getMatchingSe().getSamRevision(), ByteArrayUtil
                                    .toHex(currentSamResource.getMatchingSe().getSerialNumber()));
                }
                iterator.remove();
            }
        }
    }

    @Override
    public SamResource allocateSamResource(AllocationMode allocationMode,
            SamIdentifier samIdentifier) throws CalypsoNoSamResourceAvailableException {
        long maxBlockingDate = System.currentTimeMillis() + maxBlockingTime;
        boolean noSamResourceLogged = false;
        logger.trace("Allocating SAM reader channel...");
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
            if (allocationMode == AllocationMode.NON_BLOCKING) {
                logger.trace("No SAM resources available at the moment.");
                throw new CalypsoNoSamResourceAvailableException(
                        "No Sam resource could be allocated for samIdentifier +"
                                + samIdentifier.getGroupReference());
            } else {
                if (!noSamResourceLogged) {
                    /* log once the first time */
                    logger.trace("No SAM resources available at the moment.");
                    noSamResourceLogged = true;
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // set interrupt flag
                    logger.error("Interrupt exception in Thread.sleep.");
                }
                if (System.currentTimeMillis() >= maxBlockingDate) {
                    logger.error("The allocation process failed. Timeout {} sec exceeded .",
                            (maxBlockingTime / 1000.0));
                    throw new CalypsoNoSamResourceAvailableException(
                            "No Sam resource could be allocated within timeout of "
                                    + maxBlockingTime + "ms for samIdentifier "
                                    + samIdentifier.getGroupReference());
                }
            }
        }
    }

    @Override
    public void freeSamResource(SamResource samResource) {
        synchronized (localSamResources) {
            logger.trace("Freeing local SAM resource.");
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
                    return;
                } catch (KeypleReaderNotFoundException e) {
                    logger.error("Reader not found {}", readerName);
                    return;
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
                            try {
                                initSamReader(samReader, readerObserver);
                            } catch (KeypleReaderException e) {
                                logger.error("Unable to init Sam reader {}", samReader.getName(),
                                        e.getCause());
                            }
                        } else {
                            logger.trace("Reader not matching: {}", readerName);
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

                            logger.trace("Reader removed. READERNAME = {}", readerName);
                            if (samReader instanceof ObservableReader) {
                                if (readerObserver != null) {
                                    logger.trace(
                                            "Remove observer and stop detection READERNAME = {}",
                                            readerName);
                                    ((ObservableReader) samReader).removeObserver(readerObserver);
                                    ((ObservableReader) samReader).stopSeDetection();
                                } else {
                                    removeResource(samReader);
                                    logger.trace(
                                            "Unplugged reader READERNAME = {} wasn't observed. Resource removed.",
                                            readerName);
                                }
                            }
                        } else {
                            logger.trace("Reader not matching: {}", readerName);
                        }
                        break;
                    default:
                        logger.warn("Unexpected reader event. EVENT = {}",
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
            // TODO revise exception management
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
                        if (containsReader(localSamResources, samReader.getName())) {
                            logger.trace(
                                    "Reader is already present in the local samResources -  READERNAME = {}",
                                    samReader.getName());
                            // do nothing
                            return;
                        }

                        SamResource newSamResource = null;
                        try {
                            /*
                             * although the reader allocation is dynamic, the SAM resource type is
                             * STATIC
                             */
                            newSamResource = createSamResource(samReader);
                        } catch (CalypsoNoSamResourceAvailableException e) {
                            logger.error("Failed to create a SamResource from {}",
                                    samReader.getName());
                        }
                        /* failures are ignored */
                        if (newSamResource != null) {
                            if (logger.isInfoEnabled()) {
                                logger.trace(
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
    private static boolean containsReader(List<SamResource> samResources, String readerName) {
        for (SamResource resource : samResources) {
            if (readerName.equals(resource.getSeReader().getName())) {
                return true;
            }
        }
        return false;
    }

    private void initSamReader(SeReader samReader, ReaderObserver readerObserver)
            throws KeypleReaderException {
        samReader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO7816_3, ".*");

        /*
         * Specific to PCSC reader (no effect on Stub)
         */

        try {
            /* contactless SE works with T0 protocol */
            samReader.setParameter("protocol", "T0");

            /* Shared mode */
            samReader.setParameter("mode", "shared");
            if (samReader.isSePresent()) {
                logger.trace("Create SAM resource: {}", samReader.getName());
                synchronized (localSamResources) {
                    localSamResources.add(createSamResource(samReader));
                }
            }
        } catch (KeypleException e) {
            throw new IllegalArgumentException(
                    "Parameters are not supported for this reader : protocol:TO, mode:shared");
        }

        if (samReader instanceof ObservableReader && readerObserver != null) {
            logger.trace("Add observer and start detection READERNAME = {}", samReader.getName());
            ((ObservableReader) samReader).addObserver(readerObserver);
            ((ObservableReader) samReader).startSeDetection(ObservableReader.PollingMode.REPEATING);
        } else {
            logger.trace("Sam Reader is not an ObservableReader = {}", samReader.getName());
        }
    }
}
