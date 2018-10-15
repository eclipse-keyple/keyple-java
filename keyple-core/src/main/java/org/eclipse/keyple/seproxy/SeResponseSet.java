/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.seproxy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates the seResponses of a response from a local or remote SE Reader, received through a
 * ProxyReader, including a group of APDU responses and the previous status of the logical channel
 * with the targeted SE application.
 * 
 * @see SeRequestSet
 */
public final class SeResponseSet implements Serializable {

    static final long serialVersionUID = 125369841119873812L;


    /**
     * List of seResponses that were received following the transmission of the {@link SeRequest}.
     */
    private final List<SeResponse> seResponses;

    /**
     * List of response seResponses
     *
     * @return List of response seResponses
     */
    public List<SeResponse> getResponses() {
        return seResponses;
    }

    /**
     * Create an {@link SeResponseSet} from a list of {@link SeResponse}s.
     * 
     * @param seResponses List of seResponses
     */
    public SeResponseSet(List<SeResponse> seResponses) {
        this.seResponses = seResponses;
    }

    /**
     * Create an {@link SeResponseSet} from a single {@link SeResponse}
     *
     * @param response single {@link SeRequest}
     */
    public SeResponseSet(SeResponse response) {
        List<SeResponse> seResponses = new ArrayList<SeResponse>();
        seResponses.add(response);
        this.seResponses = seResponses;
    }

    /**
     * Return the response when the list contains only one
     *
     * @return response
     */
    public SeResponse getSingleResponse() {
        if (seResponses.size() != 1) {
            throw new IllegalStateException("This method only support ONE element");
        }
        return seResponses.get(0);
    }

    @Override
    public String toString() {
        return seResponses.size() + " SeReponse(s)";
    }
}
