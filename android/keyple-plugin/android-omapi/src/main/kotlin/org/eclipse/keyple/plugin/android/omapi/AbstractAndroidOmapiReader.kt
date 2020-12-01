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
package org.eclipse.keyple.plugin.android.omapi

import org.eclipse.keyple.core.plugin.AbstractLocalReader
import org.eclipse.keyple.core.plugin.SmartSelectionReader

/**
 * Internal abstract class extending {@link AbstractLocalReader} and implementing isContactless()
 *
 * An Android OMAPI reader does not observe smart card insersion or removal
 *
 * There is 2 implementations of AndroidOmapiReader one using org.simalliance.openmobileapi.Reader, the other
 * using android.se.omapi.
 *
 * The plugin pickup the best reader by itself.
 *
 * @since 0.9
 */
internal abstract class AbstractAndroidOmapiReader(pluginName: String, readerName: String) : AbstractLocalReader(pluginName, readerName), SmartSelectionReader {

    /**
     * The transmission mode is always CONTACTS in an OMAPI reader
     *
     * @return a boolean
     *
     * @since 0.9
     */
    override fun isContactless(): Boolean {
        return false
    }
}
