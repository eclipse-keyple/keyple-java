/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.calypso.transaction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.exception.CalypsoNoSamResourceAvailableException;
import org.eclipse.keyple.core.selection.SeResource;
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
 * Implementation of Sam Resource Manager working a {@link ReaderPlugin} (either Stub or Pcsc) It is
 * meant to work with a Keyple Pcsc Plugin or a Keyple Stub Plugin.
 */
public class SamResourceManagerDefault extends SamResourceManager {
  private static final Logger logger = LoggerFactory.getLogger(SamResourceManagerDefault.class);

  private final ConcurrentMap<String, ManagedSamResource> localManagedSamResources =
      new ConcurrentHashMap<String, ManagedSamResource>();
  final SamResourceManagerDefault.ReaderObserver readerObserver; // only used with observable
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
   *     others.
   * @param maxBlockingTime the maximum duration for which the allocateSamResource method will
   *     attempt to allocate a new reader by retrying (in milliseconds)
   * @param sleepTime the duration to wait between two retries
   * @throws KeypleReaderException thrown if an error occurs while getting the readers list.
   */
  protected SamResourceManagerDefault(
      ReaderPlugin readerPlugin, String samReaderFilter, int maxBlockingTime, int sleepTime) {
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
        "PLUGINNAME = {} initialize the localManagedSamResources with the {} connected readers filtered by {}",
        samReaderPlugin.getName(),
        samReaderPlugin.getReaders().size(),
        samReaderFilter);

    Pattern p = Pattern.compile(samReaderFilter);
    Set<String> samReadersNames = samReaderPlugin.getReaders().keySet();
    for (String samReaderName : samReadersNames) {
      if (p.matcher(samReaderName).matches()) {
        logger.trace("Add reader: {}", samReaderName);
        try {
          initSamReader(samReaderPlugin.getReader(samReaderName), readerObserver);
        } catch (KeypleReaderException e) {
          logger.error("could not init samReader {}", samReaderName, e);
        }
      } else {
        logger.trace("Reader not matching: {}", samReaderName);
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
   * Remove a {@link SeResource} from the current {@code SeResourceCalypsoSam>} list
   *
   * @param samReader the SAM reader of the resource to remove from the list.
   */
  protected void removeResource(Reader samReader) {
    ManagedSamResource managedSamResource = localManagedSamResources.get(samReader.getName());
    if (managedSamResource != null) {
      localManagedSamResources.remove(samReader.getName());
      if (logger.isInfoEnabled()) {
        logger.trace(
            "Freed SAM resource: READER = {}, SAM_REVISION = {}, SAM_SERIAL_NUMBER = {}",
            samReader.getName(),
            managedSamResource.getMatchingSe().getSamRevision(),
            ByteArrayUtil.toHex(managedSamResource.getMatchingSe().getSerialNumber()));
      }
    }
  }

  @Override
  public SeResource<CalypsoSam> allocateSamResource(
      AllocationMode allocationMode, SamIdentifier samIdentifier) {
    long maxBlockingDate = System.currentTimeMillis() + maxBlockingTime;
    boolean noSamResourceLogged = false;
    logger.trace("Allocating SAM reader channel...");
    while (true) {
      synchronized (localManagedSamResources) {
        for (Map.Entry<String, ManagedSamResource> entry : localManagedSamResources.entrySet()) {
          ManagedSamResource managedSamResource = entry.getValue();
          if (managedSamResource.isSamResourceFree()) {
            if (managedSamResource.isSamMatching(samIdentifier)) {
              managedSamResource.setSamResourceStatus(ManagedSamResource.SamResourceStatus.BUSY);
              logger.debug("Allocation succeeded. SAM resource created.");
              return managedSamResource;
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
          logger.error(
              "The allocation process failed. Timeout {} sec exceeded .",
              (maxBlockingTime / 1000.0));
          throw new CalypsoNoSamResourceAvailableException(
              "No Sam resource could be allocated within timeout of "
                  + maxBlockingTime
                  + "ms for samIdentifier "
                  + samIdentifier.getGroupReference());
        }
      }
    }
  }

  @Override
  public void freeSamResource(SeResource<CalypsoSam> samResource) {
    synchronized (localManagedSamResources) {
      ManagedSamResource managedSamResource =
          localManagedSamResources.get(samResource.getReader().getName());
      if (managedSamResource != null) {
        logger.trace("Freeing local SAM resource.");
        managedSamResource.setSamResourceStatus(ManagedSamResource.SamResourceStatus.FREE);
      } else {
        logger.error("SAM resource not found while freeing.");
      }
    }
  }

  /**
   * Plugin observer to handle SAM reader connection/disconnection.
   *
   * <p>Add or remove readers
   *
   * <p>Add a reader observer when an {@link ObservableReader} is connected.
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
        Reader samReader = null;
        logger.info(
            "PluginEvent: PLUGINNAME = {}, READERNAME = {}, EVENTTYPE = {}",
            event.getPluginName(),
            readerName,
            event.getEventType());

        /* We retrieve the reader object from its name. */
        try {
          samReader =
              SmartCardService.getInstance().getPlugin(event.getPluginName()).getReader(readerName);
        } catch (KeyplePluginNotFoundException e) {
          logger.error("Plugin not found {}", event.getPluginName());
          return;
        } catch (KeypleReaderNotFoundException e) {
          logger.error("Reader not found {}", readerName);
          return;
        }
        switch (event.getEventType()) {
          case READER_CONNECTED:
            if (localManagedSamResources.containsKey(readerName)) {
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
                logger.error("Unable to init Sam reader {}", samReader.getName(), e.getCause());
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
                  logger.trace("Remove observer and stop detection READERNAME = {}", readerName);
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
            logger.warn("Unexpected reader event. EVENT = {}", event.getEventType().name());
            break;
        }
      }
    }
  }

  /** Reader observer to handle SAM insertion/withdraw */
  class ReaderObserver implements ObservableReader.ReaderObserver {

    ReaderObserver() {
      super();
    }

    /**
     * Handle {@link ReaderEvent}
     *
     * <p>Create {@link SeResource<CalypsoSam>}
     *
     * @param event the reader event
     */
    @Override
    public void update(ReaderEvent event) {
      // TODO revise exception management
      Reader samReader = null;
      try {
        samReader = samReaderPlugin.getReader(event.getReaderName());
      } catch (KeypleReaderNotFoundException e) {
        e.printStackTrace();
      }
      synchronized (localManagedSamResources) {
        switch (event.getEventType()) {
          case SE_MATCHED:
          case SE_INSERTED:
            if (localManagedSamResources.containsKey(samReader.getName())) {
              logger.trace(
                  "Reader is already present in the local samResources -  READERNAME = {}",
                  samReader.getName());
              // do nothing
              return;
            }

            ManagedSamResource newSamResource = null;
            try {
              /*
               * although the reader allocation is dynamic, the SAM resource type is
               * STATIC
               */
              newSamResource = createSamResource(samReader);
            } catch (CalypsoNoSamResourceAvailableException e) {
              logger.error(
                  "Failed to create a SeResource<CalypsoSam> from {}", samReader.getName());
            }
            /* failures are ignored */
            if (newSamResource != null) {
              if (logger.isInfoEnabled()) {
                logger.trace(
                    "Created SAM resource: READER = {}, SAM_REVISION = {}, SAM_SERIAL_NUMBER = {}",
                    event.getReaderName(),
                    newSamResource.getMatchingSe().getSamRevision(),
                    ByteArrayUtil.toHex(newSamResource.getMatchingSe().getSerialNumber()));
              }
              localManagedSamResources.put(samReader.getName(), newSamResource);
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

  private void initSamReader(Reader samReader, ReaderObserver readerObserver) {
    /*
     * Specific to PCSC reader (no effect on Stub)
     */

    try {
      if (samReader.isSePresent()) {
        logger.trace("Create SAM resource: {}", samReader.getName());
        synchronized (localManagedSamResources) {
          localManagedSamResources.put(samReader.getName(), createSamResource(samReader));
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

  /**
   * (package-private)<br>
   * Inner class to handle specific attributes associated with an {@code SeResource<CalypsoSam>} in
   * the {@link SamResourceManager} context.
   *
   * @since 0.9
   */
  static class ManagedSamResource extends SeResource<CalypsoSam> {
    /** the free/busy enum status */
    public enum SamResourceStatus {
      FREE,
      BUSY;
    }

    /** the free/busy status of the resource */
    private SamResourceStatus samResourceStatus;

    /** the sam identifier */
    private SamIdentifier samIdentifier;

    /**
     * Constructor
     *
     * @param reader the {@link Reader} with which the card is communicating
     * @param calypsoSam the {@link CalypsoSam} information structure
     */
    public ManagedSamResource(Reader reader, CalypsoSam calypsoSam) {
      super(reader, calypsoSam);

      samResourceStatus = SamResourceStatus.FREE;
      samIdentifier = null;
    }

    /**
     * Indicates whether the ManagedSamResource is FREE or BUSY
     *
     * @return the busy status
     */
    public boolean isSamResourceFree() {
      return samResourceStatus.equals(SamResourceStatus.FREE);
    }

    /**
     * Defines the {@link SamIdentifier} of the current {@link ManagedSamResource}
     *
     * @param samIdentifier the SAM identifier
     */
    public void setSamIdentifier(SamIdentifier samIdentifier) {
      this.samIdentifier = samIdentifier;
    }

    /**
     * Indicates whether the ManagedSamResource matches the provided SAM identifier.
     *
     * <p>The test includes the {@link SamRevision}, serial number and group reference provided by
     * the {@link SamIdentifier}.
     *
     * <p>The SAM serial number can be null or empty, in this case all serial numbers are accepted.
     * It can also be a regular expression target one or more specific serial numbers.
     *
     * <p>The groupe reference can be null or empty to let all group references match but not empty
     * the group reference must match the {@link SamIdentifier} to have the method returning true.
     *
     * @param samIdentifier the SAM identifier
     * @return true or false according to the result of the correspondence test
     */
    public boolean isSamMatching(SamIdentifier samIdentifier) {
      return samIdentifier.matches(this.samIdentifier);
    }

    /**
     * Sets the free/busy status of the ManagedSamResource
     *
     * @param samResourceStatus FREE/BUSY enum value
     */
    public void setSamResourceStatus(SamResourceStatus samResourceStatus) {
      this.samResourceStatus = samResourceStatus;
    }
  }
}
