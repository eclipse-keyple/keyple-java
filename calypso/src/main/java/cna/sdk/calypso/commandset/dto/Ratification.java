package cna.sdk.calypso.commandset.dto;



/**
 * The Class Ratification.
 * In contact mode, the session is always immediately ratified.
*In contactless mode, it is possible to force the ratification by setting P1 to the value 80h.
 */
public class Ratification {
	
	/** The is ratified. */
	private boolean isRatified;

	/**
	 * Instantiates a new Ratification.
	 *
	 * @param isRatified the is ratified
	 */
	public Ratification(boolean isRatified) {
		super();
		this.isRatified = isRatified;
	}

	/**
	 * Checks if is ratified.
	 *
	 * @return true, if is ratified
	 */
	public boolean isRatified() {
		return isRatified;
	}
}
