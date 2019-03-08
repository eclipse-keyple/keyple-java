/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.seproxy.message;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Aggregates one or more SeRequests to a local or remote SE Reader, sent through a ProxyReader, in
 * order to open a logical channel with a SE application to select, and to transfer a group of APDU
 * commands to run.
 *
 * @see SeResponseSet
 */
public final class SeRequestSet implements Serializable {

    static final long serialVersionUID = 6255369841122636812L;


    /**
     * List of requests. Each {@link SeRequest} will result in a {@link SeResponse} wrapped in a
     * {@link SeResponseSet}.
     */
    private final Set<SeRequest> sortedRequests = new LinkedHashSet<SeRequest>();


    /**
     * Create an {@link SeRequestSet} from a list of {@link SeRequest}s.
     * <ul>
     * <li>A SeRequestSet could contain several SeRequest to manage the selection of different types
     * of PO application.</li>
     * <li>To exchange APDU commands with a specific selected PO application a single SeRequest is
     * necessary.</li>
     * </ul>
     *
     * @param seRequests List of {@link SeRequest}s
     */
    public SeRequestSet(Set<SeRequest> seRequests) {
        this.sortedRequests.addAll(seRequests);
    }

    /**
     * Create an {@link SeRequestSet} from a single {@link SeRequest}.
     *
     * @param seRequest single {@link SeRequest}
     */
    public SeRequestSet(SeRequest seRequest) {
        this.sortedRequests.add(seRequest);
    }

    /**
     * Add an SeRequest to the current {@link SeRequestSet}
     * 
     * @param seRequest the {@link SeRequest} to add
     */
    public void add(SeRequest seRequest) {
        this.sortedRequests.add(seRequest);
    }

    /**
     * List of requests
     *
     * @return Sorted list of requests
     */
    public Set<SeRequest> getRequests() {
        return sortedRequests;
    }

    /**
     * Return the request when the list contains only one
     *
     * @return request
     */
    public SeRequest getSingleRequest() {
        if (sortedRequests.size() != 1) {
            throw new IllegalStateException("This method only support ONE element");
        }
        return sortedRequests.iterator().next();
    }

    @Override
    public String toString() {
        return sortedRequests.size() + " SeRequest(s)";
    }
}
