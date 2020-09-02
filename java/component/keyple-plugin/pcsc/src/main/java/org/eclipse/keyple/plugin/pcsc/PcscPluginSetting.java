/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
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

/**
 * This class defines the constants useful for setting up the PC/SC plugin.<br>
 * The two current keys concern the possibility to define a pattern (regex) for recognition of a
 * player's transmission type from its name.<br>
 * These elements could possibly be used by the readers while calling getTransmissionMode if this
 * mode has not been defined otherwise.
 */
public final class PcscPluginSetting {
  /** regular expression identifying a contact reader */
  public static final String KEY_CONTACT_READER_MATCHER = "contact_reader_regex";
  /** regular expression identifying a contactless reader */
  public static final String KEY_CONTACTLESS_READER_MATCHER = "contactless_reader_regex";

  /** (private) */
  private PcscPluginSetting() {}
}
