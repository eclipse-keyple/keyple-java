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

/**
 * Indicates whether the selection process should stop after the first matching or process all
 * requests in the {@link org.eclipse.keyple.core.card.message.CardSelectionRequest} list.
 */
public enum MultiSelectionProcessing {
  /** The selection process stops as soon as a selection case is successful. */
  FIRST_MATCH,
  /** The selection process performs all the selection cases provided in the Set of CardRequest. */
  PROCESS_ALL
}
