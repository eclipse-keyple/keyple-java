/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.util;

import java.util.Map;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;

/**
 * Allow {@link ProxyReader}s and {@link ReaderPlugin}s to be named and receive configuration
 * parameters.
 */
public interface NameableConfigurable {
    /**
     *
     * @return the name of the item
     */
    String getName();

    /**
     * Gets the parameters
     *
     * @return the configuration of the item
     */
    Map<String, String> getParameters();

    /**
     * allows to define a proprietary setting for a reader or a plugin (contactless protocols
     * polling sequence, baud rate, … etc.).
     *
     * @param key the parameter key
     * @param value the parameter value
     * @throws IllegalArgumentException if the parameter or the value is not supported
     * @throws KeypleBaseException if the parameter fails to be set up
     */
    void setParameter(String key, String value)
            throws IllegalArgumentException, KeypleBaseException;

    /**
     * allows to define a set of proprietary settings for a reader or a plugin (contactless
     * protocols polling sequence, baud rate, … etc.).
     *
     * @param parameters Parameters to setup
     * @throws IllegalArgumentException if the parameters or the values is not supported
     * @throws KeypleBaseException if the parameter fails to be set up
     */
    void setParameters(Map<String, String> parameters)
            throws IllegalArgumentException, KeypleBaseException;
}
