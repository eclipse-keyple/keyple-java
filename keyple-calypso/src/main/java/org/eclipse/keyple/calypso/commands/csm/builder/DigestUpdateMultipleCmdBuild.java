/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.commands.csm.builder;

import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.commands.csm.AbstractCsmCommandBuilder;
import org.eclipse.keyple.calypso.commands.csm.CalypsoSmCommands;
import org.eclipse.keyple.calypso.commands.csm.CsmRevision;
import org.eclipse.keyple.calypso.commands.utils.RequestUtils;

// TODO: Auto-generated Javadoc
/**
 * This class provides the dedicated constructor to build the CSM Digest Update Multiple APDU
 * command.
 *
 * @author Ixxi
 *
 */
public class DigestUpdateMultipleCmdBuild extends AbstractCsmCommandBuilder {

    /** The command. */
    private static CalypsoSmCommands command = CalypsoSmCommands.DIGEST_UPDATE_MULTIPLE;

    /**
     * Instantiates a new DigestUpdateMultipleCmdBuild.
     *
     * @param revision the revision
     * @param digestData the digest data
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public DigestUpdateMultipleCmdBuild(CsmRevision revision, ByteBuffer digestData)
            throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = (byte) 0x80;
        byte p2 = (byte) 0x00;

        request = RequestUtils.constructAPDURequest(cla, command, p1, p2, digestData);
    }
}
