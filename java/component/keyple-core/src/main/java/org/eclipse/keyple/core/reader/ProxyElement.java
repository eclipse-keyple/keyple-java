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
package org.eclipse.keyple.core.reader;

import org.eclipse.keyple.core.reader.message.ProxyReader;

/**
 * (package-private)<br>
 * Allow {@link ProxyReader}s and {@link Plugin}s to be named.
 */
interface ProxyElement {

  /**
   * Gets the name of the element
   *
   * @return A not empty string.
   * @since 0.9
   */
  String getName();
}
