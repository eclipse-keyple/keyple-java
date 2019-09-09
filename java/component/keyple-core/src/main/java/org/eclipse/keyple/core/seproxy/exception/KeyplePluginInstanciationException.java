package org.eclipse.keyple.core.seproxy.exception;

public class KeyplePluginInstanciationException extends KeyplePluginException  {

    /**
     * Exception thrown when a {@link org.eclipse.keyple.core.seproxy.ReaderPlugin}
     * @param pluginName : pluginName that could not be instantiated
     */
    public KeyplePluginInstanciationException(String pluginName) {
        super("Plugin with name " + pluginName + " was not found");
    }
}
