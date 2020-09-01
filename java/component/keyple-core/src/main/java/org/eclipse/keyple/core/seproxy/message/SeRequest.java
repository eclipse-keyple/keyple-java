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
import java.util.List;
import org.eclipse.keyple.core.seproxy.SeSelector;

/**
 * List of APDU requests that will result in a {@link SeResponse}
 *
 * @see SeResponse
 */
public final class SeRequest implements Serializable {

  /** SE seSelector is either an AID or an ATR regular expression */
  private final SeSelector seSelector;

  /** contains a group of APDUCommand to operate on the selected SE application by the SE reader. */
  private final List<ApduRequest> apduRequests;

  /**
   * The constructor called by a ProxyReader in order to open a logical channel, to send a set of
   * APDU commands to a SE application, or both of them.
   *
   * @param seSelector the SeSelector containing the selection information to process the SE
   *     selection
   * @param apduRequests a optional list of {@link ApduRequest} to execute after a successful
   *     selection process
   */
  public SeRequest(SeSelector seSelector, List<ApduRequest> apduRequests) {
    this.seSelector = seSelector;
    this.apduRequests = apduRequests;
  }

  /**
   * Constructor to be used when the SE is already selected (without {@link SeSelector})
   *
   * @param apduRequests a list of ApudRequest
   */
  public SeRequest(List<ApduRequest> apduRequests) {
    this.seSelector = null;
    this.apduRequests = apduRequests;
  }

  /**
   * Gets the SE seSelector.
   *
   * @return the current SE seSelector
   */
  public SeSelector getSeSelector() {
    return seSelector;
  }

  /**
   * Gets the apdu requests.
   *
   * @return the group of APDUs to be transmitted to the SE application for this instance of
   *     SERequest.
   */
  public List<ApduRequest> getApduRequests() {
    return apduRequests;
  }

  @Override
  public String toString() {
    return String.format(
        "SeRequest:{REQUESTS = %s, SELECTOR = %s}", getApduRequests(), getSeSelector());
  }
}
