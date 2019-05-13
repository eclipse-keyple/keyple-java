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
package org.eclipse.keyple.integration.experimental.samresourcemanager;

import static org.eclipse.keyple.calypso.command.sam.SamRevision.AUTO;
import java.util.*;
import java.util.regex.Pattern;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.*;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Management of SAM resources:
 * <p>
 * Provides methods fot the allocation/deallocation of SAM resources
 */
public class SamResourceManager {
    private static final Logger logger = LoggerFactory.getLogger(SamResourceManager.class);

    public enum AllocationMode {
        BLOCKING, NON_BLOCKING
    }

    private PluginObserver pluginObserver;
    private ReaderPlugin samReaderPlugin;
    private final List<SamResource> samResources = new ArrayList<SamResource>();
    private boolean dynamicAllocationPlugin;

    /**
     * Instantiate a new SamResourceManager.
     * <p>
     * The samReaderPlugin is used to retrieve the available SAM according to the provided filter.
     * <p>
     * Setup a plugin observer if the reader plugin is observable.
     *
     * @param samReaderPlugin the plugin through which SAM readers are accessible
     * @param samReaderFilter the regular expression defining how to identify SAM readers among
     *        others.
     * @throws KeypleReaderException throw if an error occurs while getting the readers list.
     */
    public SamResourceManager(ReaderPlugin samReaderPlugin, String samReaderFilter)
            throws KeypleReaderException {
        this.samReaderPlugin = samReaderPlugin;
        if (samReaderPlugin instanceof ReaderPoolPlugin) {
            logger.info("Create SAM resource manager from reader pool plugin: {}",
                    samReaderPlugin.getName());
            // HSM reader plugin type
            dynamicAllocationPlugin = true;
        } else {
            logger.info("Create SAM resource manager from reader plugin: {}",
                    samReaderPlugin.getName());
            // Local readers plugin type
            dynamicAllocationPlugin = false;

            if (samReaderPlugin instanceof ObservablePlugin) {
                // add an observer to monitor reader and SAM insertions
                ReaderObserver readerObserver = new ReaderObserver();
                pluginObserver = new PluginObserver(readerObserver, samReaderFilter);
                logger.info("Add observer PLUGINNAME = {}", samReaderPlugin.getName());
                ((ObservablePlugin) samReaderPlugin).addObserver(this.pluginObserver);
            } else {
                // the plugin isn't observable, just add resources from the current readers if any
                logger.info("PLUGINNAME = {} isn't observable. Add available readers.",
                        samReaderPlugin.getName());
                SortedSet<? extends SeReader> samReaders = samReaderPlugin.getReaders();
                for (SeReader samReader : samReaders) {
                    String readerName = samReader.getName();
                    Pattern p = Pattern.compile(samReaderFilter);
                    if (p.matcher(readerName).matches()) {
                        logger.debug("Add reader: {}", readerName);
                        samResources.add(createSamResource(samReader));
                    } else {
                        logger.debug("Reader not matching: {}", readerName);
                    }
                }
            }
        }
    }

    private SamResource createSamResource(SeReader samReader) throws KeypleReaderException {
        SeSelection samSelection = new SeSelection();

        SamSelector samSelector = new SamSelector(new SamIdentifier(AUTO, null, null), "SAM");

        /* Prepare selector, ignore MatchingSe here */
        samSelection.prepareSelection(new SamSelectionRequest(samSelector, ChannelState.KEEP_OPEN));

        SelectionsResult selectionsResult = samSelection.processExplicitSelection(samReader);
        if (!selectionsResult.hasActiveSelection()) {
            throw new IllegalStateException("Unable to open a logical channel for SAM!");
        }
        return new SamResource(samReader,
                (CalypsoSam) selectionsResult.getActiveSelection().getMatchingSe());
    }

    /**
     * Allocate a SAM resource from the specified SAM group.
     * <p>
     * In the case where the allocation mode is BLOCKING, this method will wait until a SAM resource
     * becomes free and then return the reference to the allocated resource.
     * <p>
     * In the case where the allocation mode is NON_BLOCKING and no SAM resource is available, this
     * method will return null.
     * <p>
     * If the samGroup argument is null, the first available SAM resource will be selected and
     * returned regardless of its group.
     *
     * @param allocationMode the blocking/non-blocking mode
     * @param samIdentifier the targeted SAM identifier
     */
    public SamResource allocateSamResource(AllocationMode allocationMode,
            SamIdentifier samIdentifier) throws InterruptedException {
        if (dynamicAllocationPlugin) {
            // virtually infinite number of readers
        } else {
            // finite number of readers
            while (true) {
                synchronized (samResources) {
                    for (SamResource samResource : samResources) {
                        if (samResource.isSamResourceFree()) {
                            if (samResource.isSamMatching(samIdentifier)) {
                                samResource
                                        .setSamResourceStatus(SamResource.SamResourceStatus.BUSY);
                                return samResource;
                            }
                        }
                    }
                }
                // loop indefinitely in blocking mode, only once in non-blocking mode
                if (allocationMode == AllocationMode.NON_BLOCKING) {
                    break;
                } else {
                    // don't hog CPU resources (TODO add exit timeout)
                    logger.trace("No SAM resources " + "available at the moment.");
                    Thread.sleep(10);
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
        if (dynamicAllocationPlugin) {
            // virtually infinite number of readers
        } else {
            synchronized (samResources) {
                samResource.setSamResourceStatus(SamResource.SamResourceStatus.FREE);
            }
        }
    }

    /**
     * Remove a {@link SamResource}from the current SamResource list
     *
     * @param samReader the SAM reader of the resource to remove from the list.
     */
    private void removeResource(SeReader samReader) {
        ListIterator<SamResource> iterator = samResources.listIterator();
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

    /**
     * Plugin observer to handle SAM reader connection/disconnection.
     * <p>
     * Add or remove readers
     * <p>
     * Add a reader observer when an {@link ObservableReader} is connected.
     */
    public class PluginObserver implements ObservablePlugin.PluginObserver {

        ReaderObserver readerObserver;
        String samReaderFilter;

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
                    e.printStackTrace();
                } catch (KeypleReaderNotFoundException e) {
                    e.printStackTrace();
                }
                switch (event.getEventType()) {
                    case READER_CONNECTED:
                        logger.info("New reader! READERNAME = {}", samReader.getName());
                        /*
                         * We are informed here of a disconnection of a reader.
                         *
                         * We add an observer to this reader if possible.
                         */
                        Pattern p = Pattern.compile(samReaderFilter);
                        if (p.matcher(readerName).matches()) {
                            /* Enable logging */
                            try {
                                /* enable low level logging */
                                samReader.setParameter("logging", "true");

                                /* contactless SE works with T0 protocol */
                                samReader.setParameter("protocol", "T0");

                                /* Shared mode */
                                samReader.setParameter("mode", "shared");
                            } catch (KeypleBaseException e) {
                                e.printStackTrace();
                            }

                            if (samReader instanceof ObservableReader && readerObserver != null) {
                                logger.info("Add observer READERNAME = {}", samReader.getName());
                                ((ObservableReader) samReader).addObserver(readerObserver);
                            } else {
                                logger.info("No observer to add READERNAME = {}",
                                        samReader.getName());
                                try {
                                    if (samReader.isSePresent()) {
                                        logger.debug("Create SAM resource: {}", readerName);
                                        synchronized (samResources) {
                                            samResources.add(createSamResource(samReader));
                                        }
                                    }
                                } catch (NoStackTraceThrowable noStackTraceThrowable) {
                                    noStackTraceThrowable.printStackTrace();
                                } catch (KeypleReaderException e) {
                                    e.printStackTrace();
                                }
                            }
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
                        logger.info("Reader removed. READERNAME = {}", readerName);
                        if (samReader instanceof ObservableReader) {
                            if (readerObserver != null) {
                                logger.info("Remove observer READERNAME = {}", readerName);
                                ((ObservableReader) samReader).removeObserver(readerObserver);
                            } else {
                                removeResource(samReader);
                                logger.info(
                                        "Unplugged reader READERNAME = {} wasn't observed. Resource removed.",
                                        readerName);
                            }
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
    public class ReaderObserver implements ObservableReader.ReaderObserver {

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
            synchronized (samResources) {
                switch (event.getEventType()) {
                    case SE_MATCHED:
                    case SE_INSERTED:
                        SamResource newSamResource = null;
                        try {
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
                            samResources.add(newSamResource);
                        }
                        break;
                    case SE_REMOVAL:
                    case IO_ERROR:
                        removeResource(samReader);
                        break;
                }
            }
        }
    }
}
