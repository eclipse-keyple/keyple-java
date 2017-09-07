package org.keyple.calypso.commandset.csm;

import org.keyple.calypso.commandset.ApduCommandBuilder;
import org.keyple.calypso.commandset.CalypsoCommands;
import org.keyple.calypso.commandset.InconsistentCommandException;
import org.keyple.seproxy.APDURequest;

public abstract class CsmCommandBuilder extends ApduCommandBuilder {

    public static final CsmRevision defaultRevision = CsmRevision.S1D;//94

    protected CsmRevision csmRevision;

    public CsmCommandBuilder() {
        super();
    }

    public CsmCommandBuilder(CsmRevision revision) {
        super();
        csmRevision = (revision == null) ? defaultRevision : revision;
    }

    public CsmCommandBuilder(CalypsoCommands reference, APDURequest request) throws InconsistentCommandException {
        super(reference, request);
        if (request.getbytes() == null) {
            throw new InconsistentCommandException("null request");
        }
        if (request.getbytes().length <= 2) {
            throw new InconsistentCommandException("request too small");
        }
        if (request.getbytes()[1] != reference.getInstructionbyte()) {
            throw new InconsistentCommandException("request and reference are not consistent");
        }
    }
}
