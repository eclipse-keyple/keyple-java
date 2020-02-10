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
     * @throws KeypleReaderException throw if an error occurs while getting the readers list.
     * @return SamResourceManager working with a default plugin
     */
    static public SamResourceManager instantiate(ReaderPlugin readerPlugin, String samReaderFilter)
            throws KeypleReaderException {
        return new SamResourceManagerDefault(readerPlugin, samReaderFilter);
    }

    /**
     * Instantiate a new SamResourceManager.
     * <p>
     * The samReaderPlugin is used to retrieve the available SAM in the ReaderPoolPlugin.
     * <p>
     * Setup a plugin observer if the reader plugin is observable.
     *
     * @param samReaderPoolPlugin the plugin through which SAM readers are accessible
     * @return SamResourceManager working with a pool plugin
     */
    static public SamResourceManager instantiate(ReaderPoolPlugin samReaderPoolPlugin) {
        return new SamResourceManagerPool(samReaderPoolPlugin);
    }

}
