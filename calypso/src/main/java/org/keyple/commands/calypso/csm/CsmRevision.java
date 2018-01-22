package org.keyple.commands.calypso.csm;

/**
 * This enumeration registers all revisions of CSM.
 *
 * @author Ixxi
 */
public enum CsmRevision {

    /** The revision of C1 and S1E SAM. */
    C1(), // 00h or 80h

    /** The revision of S1D SAM SAM. */
    S1D();// 94h

    private CsmRevision() {

    }

}
