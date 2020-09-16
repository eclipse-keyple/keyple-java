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
 * The current keys concern the plugin name itself and the possibility to define a pattern (regex)
 * for recognition of a reader's transmission type from its name.<br>
 * These elements could possibly be used by the readers while calling getTransmissionMode if this
 * mode has not been defined otherwise.
 */
public final class PcscPluginConstants {
  /** The plugin name */
  public static final String PLUGIN_NAME = "PcscPlugin";

  /** regular expression identifying a contact reader */
  public static final String CONTACT_READER_MATCHER_KEY = "contact_reader_regex";
  /** regular expression identifying a contactless reader */
  public static final String CONTACTLESS_READER_MATCHER_KEY = "contactless_reader_regex";

  /** (private) */
  private PcscPluginConstants() {}
}
