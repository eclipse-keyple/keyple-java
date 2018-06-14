/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The Class SeProxyService. This singleton is the entry point of the SE Proxy Service, its instance
 * has to be called by a ticketing application in order to establish a link with a SE’s application.
 *
 * @author Ixxi
 */
public final class SeProxyService {

    /** singleton instance of SeProxyService */
    private static SeProxyService uniqueInstance = new SeProxyService();

    /** version number of the SE Proxy Service API */
    private Integer version = 1;

    /** the list of readers’ plugins interfaced with the SE Proxy Service */
    private SortedSet<ReadersPlugin> plugins = new ConcurrentSkipListSet<ReadersPlugin>();

    /**
     * Instantiates a new SeProxyService.
     */
    private SeProxyService() {}

    /**
     * Gets the single instance of SeProxyService.
     *
     * @return single instance of SeProxyService
     */
    public static SeProxyService getInstance() {
        return uniqueInstance;
    }

    /**
     * Sets the plugins.
     *
     * @param plugins the new plugins
     */
    public void setPlugins(SortedSet<ReadersPlugin> plugins) {
        this.plugins = plugins;
    }

    /**
     * Gets the plugins.
     *
     * @return the plugins the list of interfaced reader’s plugins.
     */
    public SortedSet<ReadersPlugin> getPlugins() {
        return plugins;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public Integer getVersion() {
        // TODO improve version management
        return version;
    }
}
