package org.keyple.seproxy;

import java.util.List;
import java.util.Map;

/**
 * The Interface ConfigurableReader. This object is optionally proposed by
 * plugins for readers supporting the settings of proprietary parameters.
 *
 * @author Ixxi
 */
public interface ConfigurableReader extends ProxyReader{

    /**
     * allows to define proprietary settings for a plugin (contactless protocols
     * polling sequence, baud rate, … etc.).
     *
     * @param settings
     *            the new parameters
     */
    abstract void setParameters(Map<String, String> settings);

    /**
     * allows to define proprietary settings for a plugin (contactless protocols
     * polling sequence, baud rate, … etc.).
     *
     * @param key
     *            the parameter key
     * @param value
     *            the parameter value
     */
    abstract void setAParameters(String key, String value);

    /**
     * Gets the parameters.
     *
     * @return the configuration of the selected reader.
     */
    abstract List<String> getParameters();

}