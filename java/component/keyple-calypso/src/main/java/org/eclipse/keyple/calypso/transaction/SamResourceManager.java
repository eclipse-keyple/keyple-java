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

import static org.eclipse.keyple.calypso.command.sam.SamRevision.AUTO;
import java.util.*;
import java.util.regex.Pattern;
import org.eclipse.keyple.calypso.exception.CalypsoNoSamResourceAvailableException;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
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
 * Management of SAM resources:
 * <p>
 * Provides methods fot the allocation/deallocation of SAM resources
 */
public class SamResourceManager {
    private static final Logger logger = LoggerFactory.getLogger(SamResourceManager.class);

    public enum AllocationMode {
        BLOCKING, NON_BLOCKING
    }

    /* the default maximum time (in milliseconds) during which the BLOCKING mode will wait */
    private final static int MAX_BLOCKING_TIME = 1000; // 1 sec
    private final int maxBlockingTime;
    private final ReaderPlugin samReaderPlugin;
    private final List<SamResource> localSamResources = new ArrayList<SamResource>();
    private final boolean dynamicAllocationPlugin;

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
     * @param maxBlockingTime the maximum duration for which the allocateSamResource method will
     *        attempt to allocate a new reader by retrying (in milliseconds)
     * @throws KeypleReaderException thrown if an error occurs while getting the readers list.
     * @since 0.8.1
     */
    public SamResourceManager(ReaderPlugin samReaderPlugin, String samReaderFilter,
            int maxBlockingTime) throws KeypleReaderException {
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
                PluginObserver pluginObserver = new PluginObserver(readerObserver, samReaderFilter);
                logger.info("Add observer PLUGINNAME = {}", samReaderPlugin.getName());
                ((ObservablePlugin) samReaderPlugin).addObserver(pluginObserver);
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
                        localSamResources.add(createSamResource(samReader));
                    } else {
                        logger.debug("Reader not matching: {}", readerName);
                    }
                }
            }
        }
        this.maxBlockingTime = maxBlockingTime;
    }

    /**
     * Alternate constructor with default max blocking time value
     *
     * @param samReaderPlugin the plugin through which SAM readers are accessible
     * @param samReaderFilter the regular expression defining how to identify SAM readers among
     *        others.
     * @throws KeypleReaderException thrown if an error occurs while getting the readers list.
     */
    public SamResourceManager(ReaderPlugin samReaderPlugin, String samReaderFilter)
            throws KeypleReaderException {
        this(samReaderPlugin, samReaderFilter, MAX_BLOCKING_TIME);
    }

    /**
     * Create a SAM resource from the provided SAM reader.
     * <p>
     * Proceed with the SAM selection and combine the SAM reader and the Calypso SAM resulting from
     * the selection.
     * 
     * @param samReader the SAM reader with which the APDU exchanges will be done.
     * @return a {@link SamResource}
     * @throws KeypleReaderException if an reader error occurs while doing the selection
     */
    private SamResource createSamResource(SeReader samReader) throws KeypleReaderException {
        logger.trace("Create SAM resource from reader NAME = {}", samReader.getName());

        samReader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO7816_3, ".*");

        SeSelection samSelection = new SeSelection();

        SamSelector samSelector = new SamSelector(new SamIdentifier(AUTO, null, null), "SAM");

        /* Prepare selector, ignore MatchingSe here */
        samSelection.prepareSelection(new SamSelectionRequest(samSelector));

        SelectionsResult selectionsResult = samSelection.processExplicitSelection(samReader);
        if (!selectionsResult.hasActiveSelection()) {
            throw new IllegalStateException("Unable to open a logical channel for SAM!");
        }
        CalypsoSam calypsoSam = (CalypsoSam) selectionsResult.getActiveSelection().getMatchingSe();
        return new SamResource(samReader, calypsoSam);
    }

    /**
     * Allocate a SAM resource from the specified SAM group.
     * <p>
     * In the case where the allocation mode is BLOCKING, this method will wait until a SAM resource
     * becomes free and then return the reference to the allocated resource. However, the BLOCKING
     * mode will wait a maximum time defined in tenths of a second by MAX_BLOCKING_TIME.
     * <p>
     * In the case where the allocation mode is NON_BLOCKING and no SAM resource is available, this
     * method will return null.
     * <p>
     * If the samGroup argument is null, the first available SAM resource will be selected and
     * returned regardless of its group.
     *
     * @param allocationMode the blocking/non-blocking mode
     * @param samIdentifier the targeted SAM identifier
     * @return a SAM resource
     * @throws KeypleReaderException if a reader error occurs
     * @throws CalypsoNoSamResourceAvailableException if the reader allocation failed
     */
    public SamResource allocateSamResource(AllocationMode allocationMode,
            SamIdentifier samIdentifier)
            throws KeypleReaderException, CalypsoNoSamResourceAvailableException {
        long maxBlockingDate = System.currentTimeMillis() + maxBlockingTime;
        boolean noSamResourceLogged = false;
        logger.debug("Allocating SAM reader channel...");
        while (true) {
            if (dynamicAllocationPlugin) {
                // virtually infinite number of readers
                SeReader samReader = null;
                try {
                    samReader = ((ReaderPoolPlugin) samReaderPlugin)
                            .allocateReader(samIdentifier.getGroupReference());
                } catch (KeypleAllocationReaderException e) {
                    throw new CalypsoNoSamResourceAvailableException(e.getMessage());
                }
                if (samReader != null) {
                    SamResource samResource = createSamResource(samReader);
                    logger.debug("Allocation succeeded. SAM resource created.");
                    return samResource;
                }
            } else {
                synchronized (localSamResources) {
                    for (SamResource samResource : localSamResources) {
                        if (samResource.isSamResourceFree()) {
                            if (samResource.isSamMatching(samIdentifier)) {
                                samResource
                                        .setSamResourceStatus(SamResource.SamResourceStatus.BUSY);
                                logger.debug("Allocation succeeded. SAM resource created.");
                                return samResource;
                            }
                        }
                    }
                }
            }
            // loop until MAX_BLOCKING_TIME in blocking mode, only once in non-blocking mode
            if (allocationMode == AllocationMode.NON_BLOCKING) {
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
                    throw new CalypsoNoSamResourceAvailableException(
                            "The allocation process has timed out.");
                }
            }
        }
        throw new CalypsoNoSamResourceAvailableException("The allocation process has failed.");
    }

    /**
     * Free a previously allocated SAM resource.
     *
     * @param samResource the SAM resource reference to free
     */
    public void freeSamResource(SamResource samResource) {
        if (dynamicAllocationPlugin) {
            // virtually infinite number of readers
            logger.debug("Freeing HSM SAM resource.");
            ((ReaderPoolPlugin) samReaderPlugin).releaseReader(samResource.getSeReader());
        } else {
            synchronized (localSamResources) {
                logger.debug("Freeing local SAM resource.");
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
                        logger.info("New reader! READERNAME = {}", samReader.getName());
                        /*
                         * We are informed here of a connection of a reader.
                         *
                         * We add an observer to this reader if possible.
                         */
                        p = Pattern.compile(samReaderFilter);
                        if (p.matcher(readerName).matches()) {
                            /* Enable logging */
                            try {
                                /* contactless SE works with T0 protocol */
                                samReader.setParameter("protocol", "T0");

                                /* Shared mode */
                                samReader.setParameter("mode", "shared");
                            } catch (KeypleBaseException e) {
                                logger.error("Wrong parameter", e);
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
                                        synchronized (localSamResources) {
                                            localSamResources.add(createSamResource(samReader));
                                        }
                                    }
                                } catch (KeypleIOReaderException e) {
                                    logger.error("Error in reader", e);
                                } catch (KeypleReaderException e) {
                                    logger.error("Error in reader", e);
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
                        p = Pattern.compile(samReaderFilter);
                        if (p.matcher(readerName).matches()) {

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
}
