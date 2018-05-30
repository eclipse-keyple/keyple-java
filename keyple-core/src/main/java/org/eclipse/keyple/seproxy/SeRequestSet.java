/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates one or more SeRequests to a local or remote SE Reader, sent through a ProxyReader, in
 * order to open a logical channel with a SE application to select, and to transfer a group of APDU
 * commands to run.
 *
 * @see SeResponseSet
 */
public class SeRequestSet {

    /**
     * List of requests. Each {@link SeRequest} will result in a {@link SeResponse} wrapped in a
     * {@link SeResponseSet}.
     */
    private final List<SeRequest> sortedRequests;


    /**
     * Create an {@link SeRequestSet} from a list of {@link SeRequest}s.
     * <ul>
     * <li>A SeRequestSet could contain several SeRequest to manage the selection of different types
     * of PO application.</li>
     * <li>To exchange APDU commands with a soecific selected PO application a single SeRequest is
     * necessary.</li>
     * </ul>
     *
     * @param seRequests List of {@link SeRequest}s
     */
    public SeRequestSet(List<SeRequest> seRequests) {
        this.sortedRequests = seRequests;
    }

    /**
     * Create an {@link SeRequestSet} from a single {@link SeRequest}.
     *
     * @param request single (@link SeRequest)
     */
    public SeRequestSet(SeRequest request) {
        List<SeRequest> seRequests = new ArrayList<SeRequest>();
        seRequests.add(request);
        this.sortedRequests = seRequests;
    }

    /**
     * List of requests
     *
     * @return Sorted list of requests
     */
    public List<SeRequest> getRequests() {
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
        return sortedRequests.get(0);
    }

    @Override
    public String toString() {
        return String.format("SeRequestSet{sortedRequests=%s}", getRequests());
    }
}
