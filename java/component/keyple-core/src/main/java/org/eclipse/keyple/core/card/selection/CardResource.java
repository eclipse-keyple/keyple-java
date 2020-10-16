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
package org.eclipse.keyple.core.card.selection;

import org.eclipse.keyple.core.reader.Reader;

/** The CardResource class groups a AbstractSmartCard and its associated Reader */
public class CardResource<T extends AbstractSmartCard> {
  private final Reader reader;
  private final T smartCard;

  /**
   * Constructor
   *
   * @param reader the {@link Reader} with which the card is communicating
   * @param smartCard the {@link AbstractSmartCard} information structure
   */
  public CardResource(Reader reader, T smartCard) {
    this.reader = reader;
    this.smartCard = smartCard;
  }

  /** @return the current {@link Reader} for this card */
  public Reader getReader() {
    return reader;
  }

  /** @return the {@link AbstractSmartCard} */
  public T getSmartCard() {
    return smartCard;
  }
}
