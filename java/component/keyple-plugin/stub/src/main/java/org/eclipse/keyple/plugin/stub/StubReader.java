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
package org.eclipse.keyple.plugin.stub;

import org.eclipse.keyple.core.seproxy.event.ObservableReader;

public interface StubReader extends ObservableReader {

    String ALLOWED_PARAMETER_1 = "parameter1";
    String ALLOWED_PARAMETER_2 = "parameter2";
    String CONTACTLESS_PARAMETER = "contactless";
    String CONTACTS_PARAMETER = "contacts";

    void insertSe(StubSecureElement _se);

    void removeSe();

    StubSecureElement getSe();
}
