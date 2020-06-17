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
package org.eclipse.keyple.core.seproxy;

import java.util.Map;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;

/**
 * Allow {@link ProxyReader}s and {@link ReaderPlugin}s to receive configuration parameters.
 */
public interface ProxyElement {
    /**
     * @return the unique name of the item
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
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    void setParameter(String key, String value);

    /**
     * allows to define a set of proprietary settings for a reader or a plugin (contactless
     * protocols polling sequence, baud rate, … etc.).
     *
     * @param parameters Parameters to setup
     * @throws IllegalArgumentException if the parameters or the values is not supported
     * @throws KeypleException if the parameter fails to be set up
     */
    void setParameters(Map<String, String> parameters);
}
