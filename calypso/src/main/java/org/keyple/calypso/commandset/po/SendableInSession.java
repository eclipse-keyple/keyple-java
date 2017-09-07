package org.keyple.calypso.commandset.po;

import org.keyple.seproxy.APDURequest;



/**
 * The Interface SendableInSession.
 * @author Ixxi
 */
public interface SendableInSession {

	/**
	 * Gets the APDU request.
	 *
	 * @return the APDU request
	 */
	public APDURequest getAPDURequest();
}
