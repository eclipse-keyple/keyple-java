/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.csm.parser;

import org.keyple.seproxy.ApduResponse;
import org.openjdk.jmh.annotations.Benchmark;

public class CsmGetChallengeRespParsBenchmark {

    private static final ApduResponse CHALLENGE_RAW =
            new ApduResponse("010203040506070809101112131415169000");

    @Benchmark
    public void parse() {
        CsmGetChallengeRespPars rspPars = new CsmGetChallengeRespPars(CHALLENGE_RAW);
        rspPars.getChallenge();
    }
}
