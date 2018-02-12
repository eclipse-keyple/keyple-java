/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.util.Map;

/**
 * The Interface ConfigurableReader. This object is optionally proposed by plugins for readers
 * supporting the settings of proprietary parameters.
 *
 * @author Ixxi
 */
public interface ConfigurableReader extends ProxyReader {

    /**
     * allows to define proprietary settings for a plugin (contactless protocols polling sequence,
     * baud rate, … etc.).
     *
     * @param settings the new parameters
     */
    void setParameters(Map<String, String> settings);

    /**
     * allows to define proprietary settings for a plugin (contactless protocols polling sequence,
     * baud rate, … etc.).
     *
     * @param key the parameter key
     * @param value the parameter value
     */
    void setAParameter(String key, String value);

    /**
     * Gets the parameters
     *
     * @return the configuration of the selected reader
     */
    Map<String, String> getParameters();
}
