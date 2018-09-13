package org.eclipse.keyple.seproxy.exception;

/**
 *  Used when a generic checked occurs in plugin
 */
public class KeyplePluginNotFoundException extends KeyplePluginException {

    /**
     * Exception thrown when Reader is not found
     * @param pluginName : pluginName that has not been found
     */
    public KeyplePluginNotFoundException(String pluginName) {
        super("Plugin with name "+pluginName+" was not found");
    }


}
