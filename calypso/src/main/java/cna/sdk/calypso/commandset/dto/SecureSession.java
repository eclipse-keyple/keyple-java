package cna.sdk.calypso.commandset.dto;

/**
 * The Class SecureSession.
 * A secure session is returned by a open secure session command
 */
public class SecureSession {
    
    /** The session challenge. */
    POChallenge sessionChallenge;
    
    /** The previous session ratified boolean. */
    boolean previousSessionRatified;
    
    /** The manage secure session authorized boolean. */
    boolean manageSecureSessionAuthorized;
    
    /** The kif. */
    KIF KIF;
    
    /** The kvc. */
    KVC KVC;
    
    /** The original data. */
    byte[] originalData;
    
    /** The secure session data. */
    byte[] secureSessionData;

    /**
     * Instantiates a new SecureSession for a Calypso application revision 3
     *
     * @param sessionChallenge the session challenge return by the open secure session APDU command
     * @param previousSessionRatified the previous session ratified
     * @param manageSecureSessionAuthorized the manage secure session authorized
     * @param kIF the KIF from the response of the open secure session APDU command
     * @param kVC the KVC from the response of the open secure session APDU command
     * @param originalData the original data from the response of the open secure session APDU command 
     * @param secureSessionData the secure session data from the response of open secure session APDU command
     */
    //Rev 3.1
	public SecureSession(POChallenge sessionChallenge, boolean previousSessionRatified,
            boolean manageSecureSessionAuthorized, KIF kIF, KVC kVC, byte[] originalData, byte[] secureSessionData) {
        this.sessionChallenge = sessionChallenge;
        this.previousSessionRatified = previousSessionRatified;
        this.manageSecureSessionAuthorized = manageSecureSessionAuthorized;
        this.KIF = kIF;
        this.KVC = kVC;
        this.originalData = originalData;
        this.secureSessionData = secureSessionData;
    }
	
	/**
	 * Instantiates a new SecureSession for a Calypso application revision 2.4
	 *
	 * @param sessionChallenge the session challenge return by the open secure session APDU command
     * @param previousSessionRatified the previous session ratified
     * @param manageSecureSessionAuthorized the manage secure session authorized
     * @param kVC the KVC from the response of the open secure session APDU command
     * @param originalData the original data from the response of the open secure session APDU command 
     * @param secureSessionData the secure session data from the response of open secure session APDU command
	 */
	//Rev 2.4
	public SecureSession(POChallenge sessionChallenge, boolean previousSessionRatified,
            boolean manageSecureSessionAuthorized, KVC kVC, byte[] originalData, byte[] secureSessionData) {
        this.sessionChallenge = sessionChallenge;
        this.previousSessionRatified = previousSessionRatified;
        this.manageSecureSessionAuthorized = manageSecureSessionAuthorized;
        this.KVC = kVC;
        this.originalData = originalData;
        this.secureSessionData = secureSessionData;
    }

    /**
     * Gets the session challenge.
     *
     * @return the session challenge
     */
    public POChallenge getSessionChallenge() {
        return sessionChallenge;
    }

    /**
     * Checks if is previous session ratified.
     *
     * @return the boolean
     */
    public boolean isPreviousSessionRatified() {
        return previousSessionRatified;
    }

    /**
     * Checks if is manage secure session authorized.
     *
     * @return the boolean
     */
    public boolean isManageSecureSessionAuthorized() {
        return manageSecureSessionAuthorized;
    }

    /**
     * Gets the kif.
     *
     * @return the kif
     */
    public KIF getKIF() {
        return KIF;
    }

    /**
     * Gets the kvc.
     *
     * @return the kvc
     */
    public KVC getKVC() {
        return KVC;
    }

    /**
     * Gets the original data.
     *
     * @return the original data
     */
    public byte[] getOriginalData() {
        return originalData;
    }

    /**
     * Gets the secure session data.
     *
     * @return the secure session data
     */
    public byte[] getSecureSessionData(){
    	return secureSessionData;
    }
}
