package cna.sdk.calypso.commandset.po;

import cna.sdk.seproxy.APDURequest;



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
