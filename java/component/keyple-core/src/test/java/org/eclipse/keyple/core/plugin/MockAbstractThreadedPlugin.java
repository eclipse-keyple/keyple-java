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
package org.eclipse.keyple.core.plugin;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.keyple.core.plugin.reader.AbstractReader;
import org.eclipse.keyple.core.service.Reader;

public class MockAbstractThreadedPlugin extends AbstractThreadedObservablePlugin {

  public MockAbstractThreadedPlugin(String name) {
    super(name);
  }

  public Boolean isMonitoring() {
    return super.isMonitoring();
  }

  public void finalize() throws Throwable {
    super.finalize();
  }

  @Override
  protected SortedSet<String> fetchNativeReadersNames() {
    return new TreeSet<String>();
  }

  @Override
  protected ConcurrentMap<String, Reader> initNativeReaders() {
    return new ConcurrentHashMap<String, Reader>();
  }

  @Override
  protected AbstractReader fetchNativeReader(String name) {
    return null;
  }
}