package cna.sdk.calypso.commandset.po;

import cna.sdk.calypso.commandset.ApduCommandBuilder;
import cna.sdk.calypso.commandset.CalypsoCommands;
import cna.sdk.calypso.commandset.InconsistentCommandException;
import cna.sdk.seproxy.APDURequest;

public abstract class PoCommandBuilder extends ApduCommandBuilder {

    public static final PoRevision defaultRevision = PoRevision.REV3_1;

    protected PoRevision poRevision;

    public PoCommandBuilder() {
        super();
    }

    public PoCommandBuilder(CalypsoCommands reference) {
        super(reference);
        poRevision = defaultRevision;
    }

    public PoCommandBuilder(PoRevision revision, CalypsoCommands reference) {
        super(reference);
        poRevision = (revision == null) ? defaultRevision : revision;
    }

    public PoCommandBuilder(CalypsoCommands reference, APDURequest request) throws InconsistentCommandException {
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
