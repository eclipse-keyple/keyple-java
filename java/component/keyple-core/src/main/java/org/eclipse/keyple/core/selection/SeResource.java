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
package org.eclipse.keyple.core.selection;

import org.eclipse.keyple.core.seproxy.Reader;

/** The SeResource class groups a AbstractMatchingSe and its associated Reader */
public class SeResource<T extends AbstractMatchingSe> {
  private final Reader reader;
  private final T matchingSe;

  /**
   * Constructor
   *
   * @param reader the {@link Reader} with which the card is communicating
   * @param matchingSe the {@link AbstractMatchingSe} information structure
   */
  public SeResource(Reader reader, T matchingSe) {
    this.reader = reader;
    this.matchingSe = matchingSe;
  }

  /** @return the current {@link Reader} for this card */
  public Reader getReader() {
    return reader;
  }

  /** @return the {@link AbstractMatchingSe} */
  public T getMatchingSe() {
    return matchingSe;
  }
}
