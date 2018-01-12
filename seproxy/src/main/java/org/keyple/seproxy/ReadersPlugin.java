package org.keyple.seproxy;

import java.util.List;

import org.keyple.seproxy.exceptions.IOReaderException;

/**
 * The Interface ReadersPlugin. This interface has to be implemented by each
 * plugins of readers’ drivers.
 *
 * @author Ixxi
 */
public interface ReadersPlugin {

    /**
     * Gets the name.
     *
     * @return the ‘unique’ name of the readers’ plugin.
     */
    String getName();

    /**
     * Gets the readers.
     *
     * @return the ‘unique’ name of the readers’ plugin.
     * @throws IOReaderException 
     */
    List<ProxyReader> getReaders() throws IOReaderException;
    
}