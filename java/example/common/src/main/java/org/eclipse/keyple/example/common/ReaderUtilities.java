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
package org.eclipse.keyple.example.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.regex.Pattern;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;

public final class ReaderUtilities {
  private static Properties properties;

  static {
    properties = new Properties();
    InputStream inputStream =
        ReaderUtilities.class.getClassLoader().getResourceAsStream("config.properties");
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
  private ReaderUtilities() {}

  /**
   * Sets the properties file for reader settings<br>
   * The following keys are expected:
   *
   * <ul>
   *   <li><b>reader.contact.regex</b>: regular expression matching contact readers (e.g.
   *       ".*Identive.*"
   *   <li><b>reader.contactless.regex</b>: regular expression matching contactless readers (e.g.
   *       "(.*ASK LoGo.*)|(.*Contactless.*)")
   * </ul>
   *
   * @param propertiesFile the properties file name
   * @throws IOException if the access to the file failed
   */
  public static void setPropertiesFile(String propertiesFile) throws IOException {
    // create and load properties
    properties = new Properties();
    FileInputStream in = null;
    in = new FileInputStream(propertiesFile);
    properties.load(in);
    in.close();
  }

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
    SeReader seReader = getReaderByName(filter);
    return seReader.getName();
  }

  /**
   * Get the terminal which names match the expected pattern
   *
   * @param pattern Pattern
   * @return SeReader
   * @throws KeypleReaderException the reader is not found or readers are not initialized
   */
  private static SeReader getReaderByName(String pattern) {
    Pattern p = Pattern.compile(pattern);
    Collection<ReaderPlugin> readerPlugins = SeProxyService.getInstance().getPlugins().values();
    for (ReaderPlugin plugin : readerPlugins) {
      Collection<SeReader> seReaders = plugin.getReaders().values();
      for (SeReader reader : seReaders) {
        if (p.matcher(reader.getName()).matches()) {
          return reader;
        }
      }
    }
    throw new KeypleReaderNotFoundException("Reader name pattern: " + pattern);
  }
}
