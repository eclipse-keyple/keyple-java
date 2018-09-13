package org.eclipse.keyple.seproxy.exception;

public class KeypleReaderNotFoundException extends KeypleReaderException {

    /**
     * Exception thrown when Reader is not found
     * @param readerName : readerName that has not been found
     */
    public KeypleReaderNotFoundException(String readerName) {
        super("Reader with name "+readerName+" was not found");
    }

}
