/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.util.Collections;
import java.util.List;

/**
 * Aggregates the elements of a response from a local or remote SE Reader, received through a
 * ProxyReader, including a group of APDU responses and the previous status of the logical channel
 * with the targeted SE application.
 * 
 * @see SeRequestSet
 */
public class SeResponseSet {
    /**
     * List of elements that were received following the transmission of the {@link SeRequest}.
     */
    private final List<SeResponse> elements;

    /**
     * List of response elements
     *
     * @return List of response elements
     */
    public List<SeResponse> getElements() {
        return elements;
    }

    /**
     * Compatibility layer constructor
     *
     * @deprecated You should use {@link SeResponseSet#SeResponseSet(List)} with
     *             {@link SeResponse#SeResponse(boolean, ApduResponse, List)}
     */
    public SeResponseSet(boolean channelPreviouslyOpen, ApduResponse fci,
            List<ApduResponse> apduResponses) {
        elements = Collections
                .singletonList(new SeResponse(channelPreviouslyOpen, fci, apduResponses));
    }

    /**
     * Create an {@link SeResponseSet} from a list of {@link SeResponse}s.
     * 
     * @param elements List of elements
     */
    public SeResponseSet(List<SeResponse> elements) {
        this.elements = elements;
    }

    private SeResponse getSingleElement() {
        if (elements.size() != 1) {
            throw new IllegalStateException("This method only support ONE element");
        }
        return elements.get(0);
    }

    /**
     * See {@link SeResponse#getApduResponses()}
     *
     * @deprecated Provided only as a compatibility layer with the previous architecture
     */
    @Deprecated
    public List<ApduResponse> getApduResponses() {
        return getSingleElement().getApduResponses();
    }

    /**
     * See {@link SeResponse#getFci()}
     *
     * @deprecated Provided only as a compatibility layer with the previous architecture
     */
    @Deprecated
    public ApduResponse getFci() {
        return getSingleElement().getFci();
    }

    /**
     * See {@link SeResponse#wasChannelPreviouslyOpen()}
     *
     * @deprecated Provided only as a compatibility layer with the previous architecture
     */
    @Deprecated
    public boolean wasChannelPreviouslyOpen() {
        return getSingleElement().wasChannelPreviouslyOpen();
    }

    @Override
    public String toString() {
        return "SeReponse{elements=" + elements + "}";
    }
}
