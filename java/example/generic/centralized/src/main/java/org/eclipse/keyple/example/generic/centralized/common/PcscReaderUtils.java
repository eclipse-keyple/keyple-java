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
package org.eclipse.keyple.example.generic.centralized.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.regex.Pattern;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;

public final class PcscReaderUtils {
  private static Properties properties;

  static {
    properties = new Properties();
    InputStream inputStream =
        PcscReaderUtils.class.getClassLoader().getResourceAsStream("config.properties");
    try {
      properties.load(inputStream);
      inputStream.close();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to find configuration file.");
    }
  }

  /**
   * (private)<br>
   * Constructor
   */
  private PcscReaderUtils() {}

  /**
   * Returns the name of the first reader identified as contactless according to the parameter found
   * in the properties file.
   *
   * @return the name of the contactless reader
   * @throws IllegalStateException the properties file is not set
   * @throws KeypleReaderException the reader is not found or readers are not initialized
   */
  public static String getContactlessReaderName() {
    return getReaderNameForType("reader.pcsc.contactless.regex");
  }

  /**
   * Returns the name of the first reader identified as contact according to the parameter found in
   * the properties file.
   *
   * @return the name of the contact reader
   * @throws IllegalStateException the properties file is not set
   * @throws KeypleReaderException the reader is not found or readers are not initialized
   */
  public static String getContactReaderName() {
    return getReaderNameForType("reader.pcsc.contact.regex");
  }

  /** (private) */
  private static String getReaderNameForType(String readerTypeRegex) {
    if (properties == null) {
      throw new IllegalStateException("Properties file not set.");
    }
    String filter = properties.getProperty(readerTypeRegex);
    if (filter == null) {
      throw new IllegalStateException(readerTypeRegex + " property not found.");
    }
    Reader reader = getReaderByName(filter);
    return reader.getName();
  }

  /**
   * Get the terminal which names match the expected pattern
   *
   * @param pattern Pattern
   * @return Reader
   * @throws KeypleReaderException the reader is not found or readers are not initialized
   */
  private static Reader getReaderByName(String pattern) {
    Pattern p = Pattern.compile(pattern);
    Collection<Plugin> plugins = SmartCardService.getInstance().getPlugins().values();
    for (Plugin plugin : plugins) {
      Collection<Reader> readers = plugin.getReaders().values();
      for (Reader reader : readers) {
        if (p.matcher(reader.getName()).matches()) {
          return reader;
        }
      }
    }
    throw new KeypleReaderNotFoundException("Reader name pattern: " + pattern);
  }
}
