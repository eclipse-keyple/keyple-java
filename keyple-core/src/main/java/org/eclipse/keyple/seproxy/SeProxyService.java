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
package org.eclipse.keyple.seproxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.seproxy.exception.KeyplePluginNotFoundException;

/**
 * The Class SeProxyService. This singleton is the entry point of the SE Proxy Service, its instance
 * has to be called by a ticketing application in order to establish a link with a SE’s application.
 *
 */
public final class SeProxyService {

    /** singleton instance of SeProxyService */
    private static SeProxyService uniqueInstance = new SeProxyService();

    /** the list of readers’ plugins interfaced with the SE Proxy Service */
    private SortedSet<ReaderPlugin> plugins = new ConcurrentSkipListSet<ReaderPlugin>();

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
    public void setPlugins(SortedSet<ReaderPlugin> plugins) {
        this.plugins = plugins;
    }

    /**
     * Adds a single plugin to the plugin list.
     * 
     * @param plugin the plugin to add.
     */
    public void addPlugin(ReaderPlugin plugin) {
        this.plugins.add(plugin);
    }

    /**
     * Gets the plugins.
     *
     * @return the plugins the list of interfaced reader’s plugins.
     */
    public SortedSet<ReaderPlugin> getPlugins() {
        return plugins;
    }

    /**
     * Gets the plugin whose name is provided as an argument.
     *
     * @param name the plugin name
     * @return the plugin
     * @throws KeyplePluginNotFoundException if the wanted plugin is not found
     */
    public ReaderPlugin getPlugin(String name) throws KeyplePluginNotFoundException {
        for (ReaderPlugin plugin : plugins) {
            if (plugin.getName().equals(name)) {
                return plugin;
            }
        }
        throw new KeyplePluginNotFoundException(name);
    }

    /**
     * Gets the version API, (the version of the sdk).
     *
     * @return the version
     */
    public String getVersion() {
        try {
            // load keyple core property file
            InputStream propertiesIs = this.getClass().getClassLoader()
                    .getResourceAsStream("META-INF/keyple-core.properties");
            Properties prop = new Properties();
            prop.load(propertiesIs);
            String version = prop.getProperty("version");
            if (version != null) {
                return version;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "no-version-found";
    }
}
