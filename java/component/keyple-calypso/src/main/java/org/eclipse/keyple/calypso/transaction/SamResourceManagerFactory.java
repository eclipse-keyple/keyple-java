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

import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.PoolPlugin;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;

/** Factory that builds a SamResourceManager depending on the plugin used */
public abstract class SamResourceManagerFactory {

  static final int MAX_BLOCKING_TIME = 1000; // 1 sec
  static final int DEFAULT_SLEEP_TIME = 10; // 10 ms

  /**
   * Instantiate a new SamResourceManager.
   *
   * <p>The samReaderPlugin is used to retrieve the available SAM according to the provided filter.
   *
   * <p>Setup a plugin observer if the reader plugin is observable.
   *
   * @param plugin the plugin through which SAM readers are accessible
   * @param samReaderFilter the regular expression defining how to identify SAM readers among
   *     others.
   * @param maxBlockingTime the maximum duration for which the allocateSamResource method will
   *     attempt to allocate a new reader by retrying (in milliseconds).
   * @param sleepTime the duration to wait between two retries
   * @throws KeypleReaderException throw if an error occurs while getting the readers list.
   * @return SamResourceManager working with a default plugin
   */
  public static SamResourceManager instantiate(
      Plugin plugin, String samReaderFilter, int maxBlockingTime, int sleepTime) {
    return new SamResourceManagerDefault(plugin, samReaderFilter, maxBlockingTime, sleepTime);
  }

  public static SamResourceManager instantiate(Plugin plugin, String samReaderFilter) {
    return new SamResourceManagerDefault(
        plugin, samReaderFilter, MAX_BLOCKING_TIME, DEFAULT_SLEEP_TIME);
  }

  /**
   * Instantiate a new SamResourceManager.
   *
   * <p>The samReaderPlugin is used to retrieve the available SAM in the PoolPlugin.
   *
   * <p>Setup a plugin observer if the reader plugin is observable.
   *
   * @param samPoolPlugin the plugin through which SAM readers are accessible
   * @param maxBlockingTime the maximum duration for which the allocateSamResource method will
   *     attempt to allocate a new reader by retrying (in milliseconds).
   * @param sleepTime the duration to wait between two retries
   * @return SamResourceManager working with a pool plugin
   */
  public static SamResourceManager instantiate(
      PoolPlugin samPoolPlugin, int maxBlockingTime, int sleepTime) {
    return new SamResourceManagerPool(samPoolPlugin, maxBlockingTime, sleepTime);
  }

  public static SamResourceManager instantiate(PoolPlugin samPoolPlugin) {
    return new SamResourceManagerPool(samPoolPlugin, MAX_BLOCKING_TIME, DEFAULT_SLEEP_TIME);
  }
}
