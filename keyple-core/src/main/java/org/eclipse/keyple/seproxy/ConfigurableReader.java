/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

import java.util.Map;
import org.eclipse.keyple.seproxy.exceptions.IOReaderException;

/**
 * Allow {@link ProxyReader}s to receive configuration parameters.
 */
public interface ConfigurableReader extends ProxyReader {

    /**
     * allows to define proprietary settings for a plugin (contactless protocols polling sequence,
     * baud rate, … etc.).
     *
     * @param parameters Parameters to setup
     * @throws IOReaderException Something went wrong with a parameter
     */
    void setParameters(Map<String, String> parameters) throws IOReaderException;

    /**
     * allows to define proprietary settings for a plugin (contactless protocols polling sequence,
     * baud rate, … etc.).
     *
     * @param key the parameter key
     * @param value the parameter value
     * @throws IOReaderException Something went wrong with a parameter
     */
    void setParameter(String key, String value) throws IOReaderException;

    /**
     * Gets the parameters
     *
     * @return the configuration of the selected reader
     */
    Map<String, String> getParameters();
}
