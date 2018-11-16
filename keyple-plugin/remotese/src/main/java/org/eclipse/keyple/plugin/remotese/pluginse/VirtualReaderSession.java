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
package org.eclipse.keyple.plugin.remotese.pluginse;


import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;

public interface VirtualReaderSession {

    /**
     * Retrieve sessionId
     * 
     * @return sessionId
     */
    String getSessionId();


    /**
     * Blocking transmit
     * 
     * @param nativeReaderName : local reader to transmit to
     * @param virtualReaderName : virtual reader that receives the order the transmit to
     * @param seApplicationRequest : seApplicationRequest to transmit
     * @return SeResponseSet
     */
    SeResponseSet transmit(String nativeReaderName, String virtualReaderName,
            SeRequestSet seApplicationRequest);



    /**
     * Send response in callback
     * 
     * @param seResponseSet : receive seResponseSet to be callback
     */
    void asyncSetSeResponseSet(SeResponseSet seResponseSet);

    /**
     * Has a seRequestSet in session (being transmitted)
     * 
     * @return true if a seRequestSet is being transmitted
     */
    Boolean hasSeRequestSet();

    /**
     * Get the seRequestSet being transmitted
     * 
     * @return seRequestSet transmitted
     */
    SeRequestSet getSeRequestSet();



}
