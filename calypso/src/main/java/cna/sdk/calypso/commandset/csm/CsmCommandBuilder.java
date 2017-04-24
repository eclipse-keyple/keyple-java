package cna.sdk.calypso.commandset.csm;

import cna.sdk.calypso.commandset.ApduCommandBuilder;
import cna.sdk.calypso.commandset.CalypsoCommands;
import cna.sdk.calypso.commandset.InconsistentCommandException;
import cna.sdk.seproxy.APDURequest;

public abstract class CsmCommandBuilder extends ApduCommandBuilder {

    public static final CsmRevision defaultRevision = CsmRevision.C1;

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
