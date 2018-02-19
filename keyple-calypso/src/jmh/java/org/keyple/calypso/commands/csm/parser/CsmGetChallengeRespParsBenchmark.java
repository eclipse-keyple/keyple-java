package org.keyple.calypso.commands.csm.parser;

import org.keyple.seproxy.ApduResponse;
import org.openjdk.jmh.annotations.Benchmark;

public class CsmGetChallengeRespParsBenchmark {

    private static final ApduResponse CHALLENGE_RAW = new ApduResponse("010203040506070809101112131415169000");

    @Benchmark
    public void parse() {
        CsmGetChallengeRespPars rspPars = new CsmGetChallengeRespPars(CHALLENGE_RAW);
        rspPars.getChallenge();
    }
}
