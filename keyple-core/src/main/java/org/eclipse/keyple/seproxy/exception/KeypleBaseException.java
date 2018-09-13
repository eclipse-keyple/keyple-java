package org.eclipse.keyple.seproxy.exception;


/**
 * Base Exception for all Keyple Checked Exception
 */
public class KeypleBaseException extends Exception {

    private static final long serialVersionUID = -500856379312027085L;

    /**
     * New exception to be thrown
     * @param message : message to identify the exception and the context
     */
    public KeypleBaseException(String message) {
        super(message);
    }

    /**
     * Encapsulate a lower level exception (ie CardException, IOException, HostNotFoundException..)
     * @param message
     * @param cause : lower level exception
     */
    public KeypleBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
