/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.seproxy;

import java.util.SortedSet;

/**
 * The ReaderPoolPlugin interface provides methods to handle the access to an undefined number of
 * SeReader resources.
 * <p>
 * It is typically used to define a ReaderPlugin built on top of an HSM interface that can allocate
 * a large number of virtual reader slots.
 */
public interface ReaderPoolPlugin extends ReaderPlugin {
    /**
     * Gets a list of group references that will be used as an argument to allocateReader.
     * <p>
     * A group reference can represent a family of SeReader with all the same characteristics (e.g.
     * SAM with identical key sets).
     * 
     * @return a list of String
     */
    SortedSet<String> getReaderGroupReferences();

    /**
     * Obtains an available SeReader resource and makes it exclusive to the caller until the
     * releaseReader method is called.
     * <p>
     * The allocated reader belongs to the group targeted with provided reference.
     * 
     * @param groupReference the reference of the group to which the reader belongs (may be null
     *        depending on the implementation made)
     * @return a SeReader object
     */
    SeReader allocateReader(String groupReference);

    /**
     * Releases a SeReader previously allocated with allocateReader.
     * <p>
     * This method must be called as soon as the reader is no longer needed by the caller of
     * allocateReader in order to free the resource.
     * 
     * @param seReader the SeReader to be released.
     */
    void releaseReader(SeReader seReader);
}
