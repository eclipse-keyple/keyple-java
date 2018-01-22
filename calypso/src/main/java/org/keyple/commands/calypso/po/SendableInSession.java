package org.keyple.commands.calypso.po;

import org.keyple.seproxy.ApduRequest;

/**
 * The Interface SendableInSession.
 *
 * @author Ixxi
 */
public interface SendableInSession {

    /**
     * Gets the APDU request.
     *
     * @return the APDU request
     */
    public ApduRequest getAPDURequest();
}
