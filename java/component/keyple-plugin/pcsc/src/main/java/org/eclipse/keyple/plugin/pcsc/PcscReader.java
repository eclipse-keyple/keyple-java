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

import javax.smartcardio.Card;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;

/**
 * Interface extending {@link ObservableReader}) and allowing to set the operating parameters of a
 * reader based on a PC/SC hardware interface (USB CCID).
 *
 * @since 1.0
 */
public interface PcscReader extends ObservableReader {

  /**
   * Defines the expected behavior regarding the access to the SE in a
   * multithreaded/multi-application context.
   *
   * <p>Corresponds to the beginExclusive() and endExclusive() methods of smartcard.io and, at a
   * lower level, to the connection mode defined by PC/SC and used in the SCardConnect function.
   *
   * @since 1.0
   */
  enum SharingMode {
    /** Allows simultaneous access to the SE */
    SHARED,
    /** Requests exclusive access to the SE */
    EXCLUSIVE
  }

  /**
   * Available transmission protocols as defined in the PC/SC standard.
   *
   * @since 1.0
   */
  enum IsoProtocol {

    /** to connect using any available protocol */
    ANY("*"),
    /** to connect using T=0 protocol */
    T0("T=0"),
    /** to connect using T=1 protocol */
    T1("T=1"),
    /** to connect using T=CL protocol */
    TCL("T=CL");

    private final String value;

    /**
     * Constructor.
     *
     * <p>Associates the enum value with its corresponding definition in the PC/SC standard.
     *
     * @param value A string
     */
    IsoProtocol(String value) {
      this.value = value;
    }

    /**
     * Gets the string expected by smartcard.io / PC/SC to set the SE transmission protocol.
     *
     * @return A not empty string.
     */
    public String getValue() {
      return value;
    }
  }

  /** Actions to be taken after disconnection of the SE. */
  enum DisconnectionMode {
    /** Resets the SE */
    RESET,
    /** Keeps the status of the SE unchanged */
    LEAVE
  }

  /**
   * Changes the PC/SC sharing mode (default value {@link SharingMode#EXCLUSIVE}).
   *
   * <p>This mode will be used when a new {@link Card} is created.
   *
   * <p>If a SE is already inserted, changes immediately the mode in the current {@link Card}
   * object.
   *
   * @param sharingMode The {@link SharingMode} to use (must be not null).
   * @return This instance.
   * @throws IllegalArgumentException if sharingMode is null
   * @since 1.0
   */
  PcscReader setSharingMode(SharingMode sharingMode);

  /**
   * Sets the reader transmission mode.
   *
   * <p>A PC/SC reader can be contact or contactless. There is no way by generic programming to know
   * what type of technology a reader uses.
   *
   * <p>Thus, it is the duty of the application to give the reader the means to know his own type.
   * This information will be used by the {@link SeReader#isContactless()} mode method.<br>
   * This can be achieved with this method but also by giving the plugin the means to determine the
   * type from the reader's name. In the latter case, the application does not need to call this
   * method, the reader itself will determine its type using the plugin's parameters (see {@link
   * PcscPlugin#setReaderNameFilter(boolean, String)}.
   *
   * <p>The default value for this parameter if this method is not called is undefined.<br>
   * The {@link SeReader#isContactless()} may raise an {@link IllegalStateException}.
   *
   * @param contactlessMode True to set contactless mode, false to set contact mode.
   * @return This instance.
   * @throws IllegalArgumentException if transmissionMode is null
   * @since 1.0
   */
  PcscReader setContactlessMode(boolean contactlessMode);

  /**
   * Changes the protocol to be used by the PC/SC reader when connecting to the SE ({@link
   * IsoProtocol#T0}, {@link IsoProtocol#T1}, or {@link IsoProtocol#TCL}), or {@link
   * IsoProtocol#ANY} to connect using any available protocol (default value {@link
   * IsoProtocol#ANY}).
   *
   * @param isoProtocol The {@link IsoProtocol} to use (must be not null).
   * @return This instance.
   * @throws IllegalArgumentException if isoProtocol is null
   * @since 1.0
   */
  PcscReader setIsoProtocol(IsoProtocol isoProtocol);

  /**
   * Changes the action to be taken after disconnection (default value {@link
   * DisconnectionMode#LEAVE}).
   *
   * <p>The SE is either reset or left as is.
   *
   * <p>The default value for this parameter if this method is not called is {@link
   * DisconnectionMode#LEAVE}.
   *
   * @param disconnectionMode The {@link DisconnectionMode} to use (must be not null).
   * @return This instance.
   * @throws IllegalArgumentException if disconnectionMode is null
   */
  PcscReader setDisconnectionMode(DisconnectionMode disconnectionMode);
}
