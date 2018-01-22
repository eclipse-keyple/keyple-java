package org.keyple.commands.calypso.csm;

import org.keyple.commands.calypso.ApduCommandBuilder;
import org.keyple.commands.calypso.CalypsoCommands;
import org.keyple.seproxy.ApduRequest;

/**
 *
 * This abstract class extends ApduCommandBuilder, it has to be extended by all
 * CSM command builder classes, it manages the current default revision for PO
 * commands
 *
 * @author IXXI
 *
 */
public abstract class CsmCommandBuilder extends ApduCommandBuilder {

    protected CsmRevision defaultRevision = CsmRevision.S1D;// 94

    public CsmCommandBuilder(CalypsoCommands reference, ApduRequest request) {
        super(reference, request);
    }
}
