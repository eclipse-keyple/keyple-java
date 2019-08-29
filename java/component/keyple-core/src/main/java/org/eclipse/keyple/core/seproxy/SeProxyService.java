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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SeProxyService. This singleton is the entry point of the SE Proxy Service, its instance
 * has to be called by a ticketing application in order to establish a link with a SE’s application.
 *
 */
public final class SeProxyService {

    private static final Logger logger = LoggerFactory.getLogger(SeProxyService.class);


    /** singleton instance of SeProxyService */
    private static SeProxyService uniqueInstance = new SeProxyService();

    /** the list of readers’ plugins interfaced with the SE Proxy Service */
    private Set<ReaderPlugin> plugins = new HashSet<ReaderPlugin>();

    // private SortedSet<ReaderPlugin> plugins = new ConcurrentSkipListSet<ReaderPlugin>();
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
    @Deprecated
    public void setPlugins(SortedSet<ReaderPlugin> plugins) {
        this.plugins = plugins;
    }

    /**
     * Adds a single plugin to the plugin list.
     * 
     * @param plugin the plugin to add.
     */
    @Deprecated
    public void addPlugin(ReaderPlugin plugin) {
        this.plugins.add(plugin);
    }


    /**
     * Register a new plugin to be available in the platform if not registered yet
     * 
     * @param pluginFactory : plugin factory to instanciate plugin to be added
     */
    public void registerPlugin(AbstractPluginFactory pluginFactory) {
        if (!isRegistered(pluginFactory.getPluginName())) {
            logger.info("Registering a new Plugin to the platform : {}",
                    pluginFactory.getPluginName());
            ReaderPlugin newPlugin = pluginFactory.getPluginInstance();
            this.plugins.add(newPlugin);
        } else {
            logger.warn("Plugin has already been registered to the platform : {}",
                    pluginFactory.getPluginName());
        }
    }

    /**
     * Unregister plugin from platform
     * 
     * @param pluginName : plugin name
     */
    public boolean unregisterPlugin(String pluginName) {

        ReaderPlugin readerPlugin = null;
        try {
            readerPlugin = this.getPlugin(pluginName);
            logger.info("Unregistering a plugin from the platform : {}", readerPlugin.getName());
            return plugins.remove(readerPlugin);
        } catch (KeyplePluginNotFoundException e) {
            logger.info("Plugin is not registered to the platform : {}", pluginName);
            return false;
        }

    }

    /**
     * Check weither a plugin is already registered to the platform or not
     * 
     * @param pluginName : name of the plugin to be checked
     * @return true if a plugin with matching name has been registered
     */
    public boolean isRegistered(String pluginName) {
        for (ReaderPlugin registeredPlugin : plugins) {
            if (registeredPlugin.getName().equals(pluginName)) {
                return true;
            }
        }
        return false;
    }



    /**
     * Gets the plugins.
     *
     * @return the plugins the list of interfaced reader’s plugins.
     */
    public SortedSet<ReaderPlugin> getPlugins() {
        return new TreeSet<ReaderPlugin>(plugins);
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
