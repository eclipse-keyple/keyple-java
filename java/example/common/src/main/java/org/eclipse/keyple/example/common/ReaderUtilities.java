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

import java.util.Collection;
import java.util.regex.Pattern;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReaderSetting;

public final class ReaderUtilities {
  private ReaderUtilities() {}

  /**
   * Get the terminal which names match the expected pattern
   *
   * @param pattern Pattern
   * @return SeReader
   * @throws KeypleReaderException Readers are not initialized
   */
  public static SeReader getReaderByName(String pattern) throws KeypleReaderException {
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

  /**
   * Get a fully configured contactless proxy reader
   *
   * @return the targeted SeReader to do contactless communications
   * @throws KeypleException in case of an error while retrieving the reader or setting its
   *     parameters
   */
  public static SeReader getDefaultContactLessSeReader() {
    SeReader seReader = ReaderUtilities.getReaderByName(PcscReadersSettings.PO_READER_NAME_REGEX);

    ReaderUtilities.setContactlessSettings(seReader);

    return seReader;
  }

  /**
   * Sets the reader parameters for contactless secure elements
   *
   * @param reader the reader to configure
   * @throws KeypleException in case of an error while settings the parameters
   */
  public static void setContactlessSettings(SeReader reader) {
    /* Contactless SE works with T1 protocol */
    reader.setParameter(PcscReaderSetting.KEY_PROTOCOL, PcscReaderSetting.PROTOCOL_T1);

    /*
     * PC/SC card access mode:
     *
     * The SAM is left in the SHARED mode (by default) to avoid automatic resets due to the
     * limited time between two consecutive exchanges granted by Windows.
     *
     * The PO reader is set to EXCLUSIVE mode to avoid side effects during the selection step
     * that may result in session failures.
     *
     * These two points will be addressed in a coming release of the Keyple PcSc reader plugin.
     */
    reader.setParameter(PcscReaderSetting.KEY_MODE, PcscReaderSetting.MODE_SHARED);

    // Set the transmission mode to CONTACTLESS
    reader.setParameter(
        PcscReaderSetting.KEY_TRANSMISSION_MODE, PcscReaderSetting.TRANSMISSION_MODE_CONTACTLESS);

    /* Set the PO reader protocol flag */
    reader.addSeProtocolSetting(
        SeCommonProtocols.PROTOCOL_ISO14443_4,
        PcscProtocolSetting.PCSC_PROTOCOL_SETTING.get(SeCommonProtocols.PROTOCOL_ISO14443_4));
  }

  /**
   * Sets the reader parameters for contacts secure elements
   *
   * @param reader the reader to configure
   * @throws KeypleException in case of an error while settings the parameters
   */
  public static void setContactsSettings(SeReader reader) {
    /* Contactless SE works with T0 protocol */
    reader.setParameter(PcscReaderSetting.KEY_PROTOCOL, PcscReaderSetting.PROTOCOL_T0);

    /*
     * PC/SC card access mode:
     *
     * The SAM is left in the SHARED mode (by default) to avoid automatic resets due to the
     * limited time between two consecutive exchanges granted by Windows.
     *
     * The PO reader is set to EXCLUSIVE mode to avoid side effects during the selection step
     * that may result in session failures.
     *
     * These two points will be addressed in a coming release of the Keyple PcSc reader plugin.
     */
    reader.setParameter(PcscReaderSetting.KEY_MODE, PcscReaderSetting.MODE_SHARED);

    // Set the transmission mode to CONTACT
    reader.setParameter(
        PcscReaderSetting.KEY_TRANSMISSION_MODE, PcscReaderSetting.TRANSMISSION_MODE_CONTACTS);

    // Set the SAM reader protocol flag
    reader.addSeProtocolSetting(
        SeCommonProtocols.PROTOCOL_ISO7816_3,
        PcscProtocolSetting.PCSC_PROTOCOL_SETTING.get(SeCommonProtocols.PROTOCOL_ISO7816_3));
  }
}
