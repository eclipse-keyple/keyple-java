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
package org.eclipse.keyple.core.seproxy.message;

import java.io.Serializable;

public class SelectionStatus implements Serializable {
  private final AnswerToReset atr;
  private final ApduResponse fci;
  private final boolean isMatching;

  public SelectionStatus(AnswerToReset atr, ApduResponse fci, boolean isMatching) {
    if (atr == null && fci == null) {
      throw new IllegalArgumentException("Atr and Fci can't be null at the same time.");
    }
    this.atr = atr;
    this.fci = fci;
    this.isMatching = isMatching;
  }

  public AnswerToReset getAtr() {
    return atr;
  }

  public ApduResponse getFci() {
    return fci;
  }

  public boolean hasMatched() {
    return isMatching;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SelectionStatus that = (SelectionStatus) o;

    if (isMatching != that.isMatching) return false;
    if (atr != null ? !atr.equals(that.atr) : that.atr != null) return false;
    return fci != null ? fci.equals(that.fci) : that.fci == null;
  }

  @Override
  public int hashCode() {
    int result = atr != null ? atr.hashCode() : 0;
    result = 31 * result + (fci != null ? fci.hashCode() : 0);
    result = 31 * result + (isMatching ? 1 : 0);
    return result;
  }
}
