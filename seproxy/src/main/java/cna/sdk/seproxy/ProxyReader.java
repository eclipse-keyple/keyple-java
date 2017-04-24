package cna.sdk.seproxy;

/**
 * The Interface ProxyReader. This interface has to be implemented by each
 * plugins of readers’ drivers.
 * 
 * @author Ixxi
 */
public interface ProxyReader {

	/**
	 * Gets the name.
	 *
	 * @return returns the ‘unique’ name of the SE reader for the selected
	 *         plugin.
	 */
	abstract String getName();

	/**
	 * Transmits a request to a SE application and get back the
	 * corresponding SE response o the usage of this method is conditioned to
	 * the presence of a SE in the selected reader, this method could also fail
	 * in case of IO error or wrong card state → some reader’s exceptions (SE
	 * missing, IO error, wrong card state, timeout) have to be caught during
	 * the processing of the SE request transmission.
	 *
	 * @param seApplicationRequest
	 *            the se application request
	 * @return the SE response
	 * @throws ReaderException
	 *             the reader exception
	 */
	abstract SEResponse transmit(SERequest seApplicationRequest) throws ReaderException;

	/**
	 * Checks if is SE present.
	 *
	 * @return true if a Secure Element is present in the reader
	 */
	abstract boolean isSEPresent();
}
