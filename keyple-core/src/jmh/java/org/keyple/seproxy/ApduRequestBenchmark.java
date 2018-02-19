/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

public class ApduRequestBenchmark {

    public void testAPDURequest() {
        ApduRequest request = new ApduRequest(new byte[] {(byte) 0x01, (byte) 0x02}, true);
        request.toString();
    }

}
