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
package org.eclipse.keyple.core.seproxy.plugin;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;

/** This mock plugin fails when instantiate */
public class BlankAbstractPlugin extends AbstractPlugin {

  public BlankAbstractPlugin(String name) {
    super(name);
  }

  @Override
  protected ConcurrentMap<String, Reader> initNativeReaders() throws KeypleReaderIOException {
    return new ConcurrentHashMap<String, Reader>();
  }
}
