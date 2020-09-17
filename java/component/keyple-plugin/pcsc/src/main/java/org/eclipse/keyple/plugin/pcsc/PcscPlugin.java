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

import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;

/**
 * Provides the public elements used to manage the PC/SC plugin.<br>
 * It defines in particular the type of object produced by the {@link PcscPluginFactory} and allow
 * to set parameters to retrieve the {@link TransmissionMode} from the name of a reader.
 */
public interface PcscPlugin extends ObservablePlugin {

  /**
   * Sets a filter based on regular expressions to make the plugin able to identify the {@link
   * TransmissionMode} of a reader from its name.
   *
   * <p>For example, the string ".*less.*" could identify all readers having "less" in their name as
   * contactless readers.
   *
   * <p>Names are not always as explicit, so it is sometimes better to test the brand and model.
   * Commonly used contactless readers include the "ASK LoGO" and "ACS ACR 122" models, while
   * contact readers include "Cherry TC" or "Identive".<br>
   * Thus, an application using these readers could call this method a first time with {@link
   * TransmissionMode#CONTACTLESS} and ".*(ASK LoGO|ACS ACR122).*" and a second time with {@link
   * TransmissionMode#CONTACTS} and ".*(Cherry TC|Identive).*".
   *
   * <p>Note: The use of this method is optional if the transmission mode of the readers is set
   * directly at the reader level with the {@link PcscReader#setTransmissionMode(TransmissionMode)}
   * method.<br>
   * Otherwise this method must imperatively be called twice to set the identification filters for
   * both transmission modes.
   *
   * @param transmissionMode The {@link TransmissionMode} for which the filter is to be associated
   *     (must be not null).
   * @param readerNameFilter A string a regular expression (must be not empty).
   * @throws IllegalArgumentException if transmissionMode is null or readerNameFilter is null or
   *     empty.
   * @since 1.0
   */
  void setReaderNameFilter(TransmissionMode transmissionMode, String readerNameFilter);
}
