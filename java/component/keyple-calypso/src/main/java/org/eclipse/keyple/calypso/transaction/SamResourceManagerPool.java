/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
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

import org.eclipse.keyple.calypso.exception.CalypsoNoSamResourceAvailableException;
import org.eclipse.keyple.core.selection.SeResource;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleAllocationNoReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleAllocationReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementation of Sam Resource Manager working a {@link ReaderPoolPlugin} */
public class SamResourceManagerPool extends SamResourceManager {
  private static final Logger logger = LoggerFactory.getLogger(SamResourceManagerPool.class);

  protected final ReaderPlugin samReaderPlugin;
  private final int maxBlockingTime;
  private final int sleepTime;

  /**
   * Protected constructor, use the {@link SamResourceManagerFactory}
   *
   * @param samReaderPoolPlugin the reader pool plugin
   * @param maxBlockingTime the maximum duration for which the allocateSamResource method will
   *     attempt to allocate a new reader by retrying (in milliseconds).
   * @param sleepTime the duration to wait between two retries
   */
  protected SamResourceManagerPool(
      ReaderPoolPlugin samReaderPoolPlugin, int maxBlockingTime, int sleepTime) {
    if (sleepTime < 1) {
      throw new IllegalArgumentException("Sleep time must be greater than 0");
    }
    if (maxBlockingTime < 1) {
      throw new IllegalArgumentException("Max Blocking Time must be greater than 0");
    }
    this.sleepTime = sleepTime;
    this.maxBlockingTime = maxBlockingTime;
    this.samReaderPlugin = samReaderPoolPlugin;
    logger.info(
        "Create SAM resource manager from reader pool plugin: {}", samReaderPlugin.getName());
    // HSM reader plugin type
  }

  /** {@inheritDoc} */
  @Override
  public SeResource<CalypsoSam> allocateSamResource(
      AllocationMode allocationMode, SamIdentifier samIdentifier) {
    long maxBlockingDate = System.currentTimeMillis() + maxBlockingTime;
    boolean noSamResourceLogged = false;
    logger.debug("Allocating SAM reader channel...");
    while (true) {
      try {
        // virtually infinite number of readers
        SeReader samReader =
            ((ReaderPoolPlugin) samReaderPlugin).allocateReader(samIdentifier.getGroupReference());
        if (samReader != null) {
          SamResourceManagerDefault.ManagedSamResource managedSamResource =
              createSamResource(samReader);
          logger.debug("Allocation succeeded. SAM resource created.");
          return managedSamResource;
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
      } catch (KeypleAllocationReaderException e) {
        throw new KeypleAllocationReaderException(
            "Allocation failed due to a plugin technical error", e);
      } catch (KeypleAllocationNoReaderException e) {
        // no reader is available, let's retry
      }
    }
  }

  @Override
  public void freeSamResource(SeResource<CalypsoSam> samResource) {
    // virtually infinite number of readers
    logger.debug("Freeing HSM SAM resource.");
    ((ReaderPoolPlugin) samReaderPlugin).releaseReader(samResource.getSeReader());
  }
}
