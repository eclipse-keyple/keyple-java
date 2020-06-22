package org.eclipse.keyple.plugin.remotese.nativese.impl;

import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.plugin.remotese.core.impl.AbstractKeypleMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for NativeSeSeService
 * This is an internal class an must not be used by the user.
 * @since 1.0
 */
abstract class AbstractNativeSeService extends AbstractKeypleMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNativeSeService.class);

    /**
     * Find a local reader among all plugins
     * @param nativeReaderName : name of the reader to be found
     * @return Se Reader found if any
     * @throws KeypleReaderNotFoundException if not reader were found with this name
     */
    public SeReader findLocalReader(String nativeReaderName) throws KeypleReaderNotFoundException{

        logger.trace("Find local reader by name {} in {} plugin(s)", nativeReaderName,
                SeProxyService.getInstance().getPlugins().size());

        for (ReaderPlugin plugin : SeProxyService.getInstance().getPlugins().values()) {
            try {
                logger.trace("Local reader found {} in plugin {}", nativeReaderName,
                        plugin.getName());
                return plugin.getReader(nativeReaderName);
            } catch (KeypleReaderNotFoundException e) {
                //reader has not been found in this plugin, continue
            }
        }
        throw new KeypleReaderNotFoundException(nativeReaderName);
    }


}
