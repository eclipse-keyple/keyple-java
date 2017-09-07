package org.keyple.seproxy;

import java.util.List;


/**
 * The Class SEResponse. This class aggregates the elements of a response from a
 * local or remote SE Reader, received through a ProxyReader, including a group
 * of APDU responses and the previous status of the logical channel with the
 * targeted SE application.
 *
 * @author Ixxi
 */
public class SEResponse {

	/**
	 * is defined as true by the SE reader in case a logical channel was already
	 * open with the target SE application.
	 */
	private boolean channelPreviouslyOpen;

	/**
	 * present if channelPreviouslyOpen is false, contains the FCI response of
	 * the channel opening (either the response of a SelectApplication command,
	 * or the response of a GetData(‘FCI’) command).
	 */
	private APDUResponse fci;

	/**
	 * could contain a group of APDUResponse returned by the selected SE
	 * application on the SE reader.
	 */
	private List<APDUResponse> apduResponses;

	/**
	 * the constructor called by a ProxyReader during the processing of the
	 * ‘transmit’ method.
	 *
	 * @param channelPreviouslyOpen
	 *            the channel previously open
	 * @param fci
	 *            the fci data
	 * @param apduResponses
	 *            the apdu responses
	 */
	public SEResponse(boolean channelPreviouslyOpen, APDUResponse fci, List<APDUResponse> apduResponses) {
		this.channelPreviouslyOpen = channelPreviouslyOpen;
		this.fci = null;
		if(this.channelPreviouslyOpen){
			this.fci = fci;
		}
		
		this.apduResponses = apduResponses;
	}

	/**
	 * Was channel previously open.
	 *
	 * @return the previous state of the logical channel.
	 */
	public boolean wasChannelPreviouslyOpen() {
		return channelPreviouslyOpen;
	}

	/**
	 * Gets the fci data.
	 *
	 * @return null or the FCI response if a channel was opened.
	 */
	public APDUResponse getFci() {
//		if (wasChannelPreviouslyOpen()) {
			return this.fci;
//		} else {
//			return null;
//		}
	}

	/**
	 * Gets the apdu responses.
	 *
	 * @return the group of APDUs responses returned by the SE application for
	 *         this instance of SEResponse.
	 */
	public List<APDUResponse> getApduResponses() {
		return apduResponses;
	}

}