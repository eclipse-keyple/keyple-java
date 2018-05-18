/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates the elements of a request to a local or remote SE Reader, sent through a ProxyReader,
 * in order to open a logical channel with a SE application to select, and to transfer a group of
 * APDU commands to run.
 *
 * @see SeResponseSet
 */
public class SeRequestSet {

    /**
     * List of request elements. Each {@link SeRequest} will result in a {@link SeResponse} wrapped
     * in a {@link SeResponseSet}.
     */
    private final List<SeRequest> elements;

    /**
     * the constructor called by a ProxyReader in order to open a logical channel, to send a set of
     * APDU commands to a SE application, or both of them.
     *
     * @param aidToSelect the aid to select
     * @param apduRequests the apdu requests
     * @param keepChannelOpen the keep channel open
     * @deprecated Only provided as a compatibility layer. You should now use
     *             {@link SeRequestSet#SeRequestSet(List)} constructor with a list of
     *             {@link SeRequest}s.
     */
    @Deprecated
    public SeRequestSet(ByteBuffer aidToSelect, List<ApduRequest> apduRequests,
            boolean keepChannelOpen) {
        elements = Collections
                .singletonList(new SeRequest(aidToSelect, apduRequests, keepChannelOpen));
    }

    /**
     * @param apduRequests list of APDU requests
     * @deprecated Only provided as a compatibility layer. You should now use
     *             {@link SeRequestSet#SeRequestSet(List)} constructor with a list of
     *             {@link SeRequest}s.
     */
    // florent: #82: I had to transform the constructor into a helper method because there
    // was a signature conflict.
    public static SeRequestSet fromApduRequests(List<ApduRequest> apduRequests) {
        return new SeRequestSet(Collections.singletonList(new SeRequest(apduRequests)));
    }

    /**
     * Constructor
     *
     * @param elements List of {@link SeRequest}s
     */
    public SeRequestSet(List<SeRequest> elements) {
        this.elements = elements;
    }

    /**
     * List of request elements
     *
     * @return List of request elements
     */
    public List<SeRequest> getElements() {
        return elements;
    }

    private SeRequest getSingleElement() {
        if (elements.size() != 1) {
            throw new IllegalStateException("This method only support ONE element");
        }
        return elements.get(0);
    }

    /**
     * See {@link SeRequest#getApduRequests()}
     *
     * @deprecated Provided only as a compatibility layer with the previous architecture
     */
    @Deprecated
    public List<ApduRequest> getApduRequests() {
        return getSingleElement().getApduRequests();
    }

    /**
     * See {@link SeRequest#getAidToSelect()}
     *
     * @deprecated Provided only as a compatibility layer with the previous architecture
     */
    @Deprecated
    public ByteBuffer getAidToSelect() {
        return getSingleElement().getAidToSelect();
    }

    /**
     * See {@link SeRequest#keepChannelOpen()}
     *
     * @deprecated Provided only as a compatibility layer with the previous architecture
     */
    @Deprecated
    public boolean keepChannelOpen() {
        return getSingleElement().keepChannelOpen();
    }

    @Override
    public String toString() {
        return String.format("SeRequestSet{elements=%s}", getElements());
    }
}
