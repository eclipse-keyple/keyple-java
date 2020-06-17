/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
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


import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;

/**
 * Factory that builds a SamResourceManager depending on the plugin used
 */
public abstract class SamResourceManagerFactory {

    final static int MAX_BLOCKING_TIME = 1000; // 1 sec
    final static int DEFAULT_SLEEP_TIME = 10; // 10 ms

    /**
     * Instantiate a new SamResourceManager.
     * <p>
     * The samReaderPlugin is used to retrieve the available SAM according to the provided filter.
     * <p>
     * Setup a plugin observer if the reader plugin is observable.
     *
     * @param readerPlugin the plugin through which SAM readers are accessible
     * @param samReaderFilter the regular expression defining how to identify SAM readers among
     *        others.
     * @param maxBlockingTime the maximum duration for which the allocateSamResource method will
     *        attempt to allocate a new reader by retrying (in milliseconds).
     * @param sleepTime the duration to wait between two retries
     * @throws KeypleReaderException throw if an error occurs while getting the readers list.
     * @return SamResourceManager working with a default plugin
     */
    public static SamResourceManager instantiate(ReaderPlugin readerPlugin, String samReaderFilter,
            int maxBlockingTime, int sleepTime) {
        return new SamResourceManagerDefault(readerPlugin, samReaderFilter, maxBlockingTime,
                sleepTime);
    }

    public static SamResourceManager instantiate(ReaderPlugin readerPlugin,
            String samReaderFilter) {
        return new SamResourceManagerDefault(readerPlugin, samReaderFilter, MAX_BLOCKING_TIME,
                DEFAULT_SLEEP_TIME);
    }

    /**
     * Instantiate a new SamResourceManager.
     * <p>
     * The samReaderPlugin is used to retrieve the available SAM in the ReaderPoolPlugin.
     * <p>
     * Setup a plugin observer if the reader plugin is observable.
     *
     * @param samReaderPoolPlugin the plugin through which SAM readers are accessible
     * @param maxBlockingTime the maximum duration for which the allocateSamResource method will
     *        attempt to allocate a new reader by retrying (in milliseconds).
     * @param sleepTime the duration to wait between two retries
     * @return SamResourceManager working with a pool plugin
     */
    public static SamResourceManager instantiate(ReaderPoolPlugin samReaderPoolPlugin,
            int maxBlockingTime, int sleepTime) {
        return new SamResourceManagerPool(samReaderPoolPlugin, maxBlockingTime, sleepTime);
    }

    public static SamResourceManager instantiate(ReaderPoolPlugin samReaderPoolPlugin) {
        return new SamResourceManagerPool(samReaderPoolPlugin, MAX_BLOCKING_TIME,
                DEFAULT_SLEEP_TIME);
    }

}
