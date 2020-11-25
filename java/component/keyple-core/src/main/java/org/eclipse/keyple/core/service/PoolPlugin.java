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
package org.eclipse.keyple.core.service;

import java.util.SortedSet;
import org.eclipse.keyple.core.service.exception.KeypleAllocationNoReaderException;
import org.eclipse.keyple.core.service.exception.KeypleAllocationReaderException;

/**
 * The PoolPlugin interface provides methods to handle the access to an undefined number of Reader
 * resources.
 *
 * <p>It is typically used to define a Plugin built on top of an HSM interface that can allocate a
 * large number of virtual reader slots.
 *
 * <p>A PoolPlugin can't be observable.
 */
public interface PoolPlugin extends Plugin {
  /**
   * Gets a list of group references that will be used as an argument to allocateReader.
   *
   * <p>A group reference can represent a family of Reader with all the same characteristics (e.g.
   * SAM with identical key sets).
   *
   * @return a list of String
   */
  SortedSet<String> getReaderGroupReferences();

  /**
   * Obtains an available Reader resource and makes it exclusive to the caller until the
   * releaseReader method is called.
   *
   * <p>The allocated reader belongs to the group targeted with provided reference.
   *
   * @param groupReference the reference of the group to which the reader belongs (may be null
   *     depending on the implementation made)
   * @return a Reader object
   * @throws KeypleAllocationReaderException if the allocation failed due to a technical error
   * @throws KeypleAllocationNoReaderException if the allocation failed due to lack of available
   *     reader
   */
  Reader allocateReader(String groupReference);

  /**
   * Releases a Reader previously allocated with allocateReader.
   *
   * <p>This method must be called as soon as the reader is no longer needed by the caller of
   * allocateReader in order to free the resource.
   *
   * @param reader the Reader to be released.
   */
  void releaseReader(Reader reader);
}
