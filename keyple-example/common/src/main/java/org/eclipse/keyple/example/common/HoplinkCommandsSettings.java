/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface HoplinkCommandsSettings {
    String AID = "A000000291A000000191";
    Set<Short> selectApplicationSuccessfulStatusCodes =
            new HashSet<Short>(Arrays.asList((short) 0x6283));
}
