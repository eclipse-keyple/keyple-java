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
package org.eclipse.keyple.core.seproxy;

import java.util.Map;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.util.Configurable;
import org.eclipse.keyple.core.util.Nameable;

/**
 * This abstract class mutualizes the management of the name and part of the configuration of
 * SeProxy components (plugins and readers)
 */
public abstract class AbstractSeProxyComponent implements Nameable, Configurable {

    private final String name;

    /**
     * Constructor
     * 
     * @param name the name of the component
     */
    public AbstractSeProxyComponent(String name) {
        this.name = name;
    }

    /**
     * @return the name of the component
     */
    @Override
    public final String getName() {
        return name;
    }

    /**
     * Sets at once a set of parameters for a reader
     * <p>
     * See {@link #setParameter(String, String)} for more details
     *
     * @param parameters a Map &lt;String, String&gt; parameter set
     * @throws KeypleBaseException if one of the parameters could not be set up
     */
    @Override
    public final void setParameters(Map<String, String> parameters) throws KeypleBaseException {
        for (Map.Entry<String, String> en : parameters.entrySet()) {
            setParameter(en.getKey(), en.getValue());
        }
    }
}
