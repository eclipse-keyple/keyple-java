/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.util;

import java.util.Map;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.message.ProxyReader;

/**
 * Allow {@link ProxyReader}s and {@link ReaderPlugin}s to receive configuration parameters.
 */
public interface Configurable {

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
