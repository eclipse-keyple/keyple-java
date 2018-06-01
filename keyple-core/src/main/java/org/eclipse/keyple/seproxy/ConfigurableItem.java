/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;


import java.io.IOException;
import java.util.Map;

/**
 * Allow {@link ProxyReader}s and {@link ReadersPlugin}s to receive configuration parameters.
 */
public interface ConfigurableItem {

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
     */
    void setParameter(String key, String value) throws IOException;

    /**
     * allows to define a set of proprietary settings for a reader or a plugin (contactless
     * protocols polling sequence, baud rate, … etc.).
     *
     * @param parameters Parameters to setup
     */
    void setParameters(Map<String, String> parameters) throws IOException;
}
