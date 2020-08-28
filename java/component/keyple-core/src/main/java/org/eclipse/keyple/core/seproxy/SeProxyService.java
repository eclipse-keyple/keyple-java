/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.seproxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstantiationException;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SeProxyService. This singleton is the entry point of the SE Proxy Service, its instance
 * has to be called by a ticketing application in order to establish a link with a SE’s application.
 */
public final class SeProxyService {

  private static final Logger logger = LoggerFactory.getLogger(SeProxyService.class);

  /** singleton instance of SeProxyService */
  private static SeProxyService uniqueInstance = new SeProxyService();

  /** the list of readers’ plugins interfaced with the SE Proxy Service */
  private final Map<String, ReaderPlugin> plugins = new ConcurrentHashMap();

  // this is the object we will be synchronizing on ("the monitor")
  private final Object MONITOR = new Object();

  /** Instantiates a new SeProxyService. */
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
   * Register a new plugin to be available in the platform if not registered yet
   *
   * @param pluginFactory : plugin factory to instantiate plugin to be added
   * @throws KeyplePluginInstantiationException if instantiation failed
   * @return ReaderPlugin : registered reader plugin
   */
  public ReaderPlugin registerPlugin(PluginFactory pluginFactory) {

    if (pluginFactory == null) {
      throw new IllegalArgumentException("Factory must not be null");
    }

    synchronized (MONITOR) {
      final String pluginName = pluginFactory.getPluginName();
      if (this.plugins.containsKey(pluginName)) {
        logger.warn("Plugin has already been registered to the platform : {}", pluginName);
        return this.plugins.get(pluginName);
      } else {
        ReaderPlugin pluginInstance = pluginFactory.getPlugin();
        logger.info("Registering a new Plugin to the platform : {}", pluginName);
        this.plugins.put(pluginName, pluginInstance);
        return pluginInstance;
      }
    }
  }

  /**
   * Unregister plugin from platform
   *
   * @param pluginName : plugin name
   * @return true if the plugin was successfully unregistered
   */
  public boolean unregisterPlugin(String pluginName) {
    synchronized (MONITOR) {
      final ReaderPlugin removedPlugin = plugins.remove(pluginName);
      if (removedPlugin != null) {
        logger.info("Unregistering a plugin from the platform : {}", removedPlugin.getName());
      } else {
        logger.warn("Plugin is not registered to the platform : {}", pluginName);
      }
      return removedPlugin != null;
    }
  }

  /**
   * Check weither a plugin is already registered to the platform or not
   *
   * @param pluginName : name of the plugin to be checked
   * @return true if a plugin with matching name has been registered
   */
  public synchronized boolean isRegistered(String pluginName) {
    synchronized (MONITOR) {
      return plugins.containsKey(pluginName);
    }
  }

  /**
   * Gets the plugins.
   *
   * @return the plugin names and plugin instances map of interfaced reader’s plugins.
   */
  public synchronized Map<String, ReaderPlugin> getPlugins() {
    return plugins;
  }

  /**
   * Gets the plugin whose name is provided as an argument.
   *
   * @param name the plugin name
   * @return the plugin
   * @throws KeyplePluginNotFoundException if the wanted plugin is not found
   */
  public synchronized ReaderPlugin getPlugin(String name) {
    synchronized (MONITOR) {
      ReaderPlugin readerPlugin = plugins.get(name);
      if (readerPlugin == null) {
        throw new KeyplePluginNotFoundException(name);
      }
      return readerPlugin;
    }
  }

  /**
   * Gets the version API, (the version of the sdk).
   *
   * @return the version
   */
  public String getVersion() {
    try {
      // load keyple core property file
      InputStream propertiesIs =
          Thread.currentThread()
              .getContextClassLoader()
              .getResourceAsStream("META-INF/keyple-core.properties");
      Properties prop = new Properties();
      prop.load(propertiesIs);
      String version = prop.getProperty("version");
      if (version != null) {
        return version;
      }
      propertiesIs.close();
    } catch (IOException e) {
      logger.error("Keyple core properties file not found in META_INF");
    }

    return "no-version-found";
  }
}
