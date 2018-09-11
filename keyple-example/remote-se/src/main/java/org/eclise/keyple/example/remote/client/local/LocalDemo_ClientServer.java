/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.client.local;

public class LocalDemo_ClientServer {


    public static void main(String[] args) throws Exception {


        LocalServerTicketingApp server = new LocalServerTicketingApp();
        server.boot();

        LocalClientReaderApp client = new LocalClientReaderApp();
        client.boot();
        client.demo();

        server.status();


    }
}
