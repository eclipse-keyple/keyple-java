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
 * @see SeResponse
 */
public class SeRequest {

    /**
     * Name of the target for which this request is dedicated. This string is used to select the
     * filter defined by the PROTOCOLS MAP parameter.
     */
    private String targetSelector;

    /**
     * List of request elements. Each {@link SeRequestElement} will result in a
     * {@link SeResponseElement} wrapped in a {@link SeResponse}.
     */
    private final List<SeRequestElement> elements;

    /**
     * the constructor called by a ProxyReader in order to open a logical channel, to send a set of
     * APDU commands to a SE application, or both of them.
     *
     * @param aidToSelect the aid to select
     * @param apduRequests the apdu requests
     * @param keepChannelOpen the keep channel open
     * @deprecated Only provided as a compatibility layer. You should now use
     *             {@link SeRequest#SeRequest(List)} constructor with a list of
     *             {@link SeRequestElement}s.
     */
    @Deprecated
    public SeRequest(ByteBuffer aidToSelect, List<ApduRequest> apduRequests,
            boolean keepChannelOpen) {
        elements = Collections
                .singletonList(new SeRequestElement(aidToSelect, apduRequests, keepChannelOpen));
    }

    /**
     * @param apduRequests list of APDU requests
     * @deprecated Only provided as a compatibility layer. You should now use
     *             {@link SeRequest#SeRequest(List)} constructor with a list of
     *             {@link SeRequestElement}s.
     */
    // florent: #82: I had to transform the constructor into a helper method because there
    // was a signature conflict.
    public static SeRequest fromApduRequests(List<ApduRequest> apduRequests) {
        return new SeRequest(Collections.singletonList(new SeRequestElement(apduRequests)));
    }

    /**
     * Constructor
     *
     * @param elements List of {@link SeRequestElement}s
     */
    public SeRequest(List<SeRequestElement> elements) {
        this.elements = elements;
    }

    /**
     * List of request elements
     *
     * @return List of request elements
     */
    public List<SeRequestElement> getElements() {
        return elements;
    }

    private SeRequestElement getSingleElement() {
        if (elements.size() != 1) {
            throw new IllegalStateException("This method only support ONE element");
        }
        return elements.get(0);
    }

    /**
     * See {@link SeRequestElement#getApduRequests()}
     *
     * @deprecated Provided only as a compatibility layer with the previous architecture
     */
    @Deprecated
    public List<ApduRequest> getApduRequests() {
        return getSingleElement().getApduRequests();
    }

    /**
     * See {@link SeRequestElement#getAidToSelect()}
     *
     * @deprecated Provided only as a compatibility layer with the previous architecture
     */
    @Deprecated
    public ByteBuffer getAidToSelect() {
        return getSingleElement().getAidToSelect();
    }

    /**
     * See {@link SeRequestElement#keepChannelOpen()}
     *
     * @deprecated Provided only as a compatibility layer with the previous architecture
     */
    @Deprecated
    public boolean keepChannelOpen() {
        return getSingleElement().keepChannelOpen();
    }

    /**
     * Set the answer filter mask
     *
     * @param targetSelector
     */
    public void setTargetSelector(String targetSelector) {
        this.targetSelector = targetSelector;
    }

    /**
     * Get the answer filter mask
     *
     * @return the mask string
     */
    public String getTargetSelector() {
        return targetSelector;
    }

    @Override
    public String toString() {
        return String.format("SeRequest{elements=%s}", getElements());
    }
}
