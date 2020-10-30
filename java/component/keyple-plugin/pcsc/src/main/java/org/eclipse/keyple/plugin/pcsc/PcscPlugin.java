/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.pcsc;

import org.eclipse.keyple.core.service.event.ObservablePlugin;

/**
 * Provides the public elements used to manage the PC/SC plugin.<br>
 * It defines in particular the type of object produced by the {@link PcscPluginFactory} and allow
 * to set parameters to retrieve the communication mode from the name of a reader.
 */
public interface PcscPlugin extends ObservablePlugin {

  /**
   * Sets a filter based on regular expressions to make the plugin able to identify the
   * communication mode of a card reader from its name.
   *
   * <p>For example, the string ".*less.*" could identify all readers having "less" in their name as
   * contactless readers.
   *
   * <p>Names are not always as explicit, so it is sometimes better to test the brand and model.
   * Commonly used contactless readers include the "ASK LoGO" and "ACS ACR 122" models, while
   * contact readers include "Cherry TC" or "Identive".<br>
   * Thus, an application using these readers could call this method a first time with <code>true
   * </code> and ".*(ASK LoGO|ACS ACR122).*" and a second time with <code>false</code> and
   * ".*(Cherry TC|Identive).*".
   *
   * <p>Note: The use of this method is optional if the transmission mode of the readers is set
   * directly at the reader level with the {@link PcscReader#setContactless(boolean)} method. <br>
   * Otherwise this method must imperatively be called twice to set the identification filters for
   * both transmission modes.
   *
   * @param contactlessMode True if the the filter is to be associated with contactless readers,
   *     false if not.
   * @param readerNameFilter A string a regular expression (must be not empty).
   * @since 1.0
   */
  void setReaderNameFilter(boolean contactlessMode, String readerNameFilter);

  /**
   * Sets a protocol identification rule based on an ATR analysis.
   *
   * <p>The rule is a regular expression contained in a String.
   *
   * <ul>
   *   <li>If a rule already exists for the provided protocol, it is replaced.
   *   <li>If there is no rule for the provided protocol, it is added.
   * </ul>
   *
   * Note: A predefined list of identification rules is already integrated (see {@link
   * PcscProtocolSetting}), however, depending on the type of reader used the ATR returned when
   * presenting contactless cards may vary. It is therefore important to be able to update these
   * rules.
   *
   * @param readerProtocolName A not empty String.
   * @param protocolRule A not empty String.
   * @since 1.0
   */
  void setProtocolIdentificationRule(String readerProtocolName, String protocolRule);
}
