package org.keyple.seproxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SEProxyService. This singleton is the entry point of the SE Proxy
 * Service, its instance has to be called by a ticketing application in order to
 * establish a link with a SE’s application.
 *
 * @author Ixxi
 */
public final class SEProxyService {

	/** The Constant logger. */
	static final Logger logger = LoggerFactory.getLogger(SEProxyService.class);

	/** singleton’s instance of SEProxyService */
	private static SEProxyService uniqueInstance = new SEProxyService();

	/** version number of the SE Proxy Service API */
	private Integer version = 1;

	/** the list of readers’ plugins interfaced with the SE Proxy Service */
	private List<ReadersPlugin> plugins = new ArrayList<>();

	private Properties prop;

	/**
	 * Instantiates a new SEProxyService.
	 */
	private SEProxyService() {
		InputStream input = null;
		String[] stringTable = null;
		if (this.prop == null) {
			
			try {
				this.prop = System.getProperties();
				for (Entry<Object, Object> s : this.prop.entrySet()) {
					logger.info("System.Property : " + s.getKey() + " " + s.getValue());
				}
				if (System.getProperty("application.properties") != null) {
					input = new FileInputStream(System.getProperty("application.properties"));

					this.prop.load(input);
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		stringTable = this.prop.getProperty("calypso.available.plugins").split(",");

		for (String string : stringTable) {
			Class<?> plugin = null;
			Object reader = null;
			try {
				plugin = Class.forName(string);
				reader = plugin.newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException
					| NullPointerException e) {

				logger.error(e.getMessage(), e);
			}
			plugins.add((ReadersPlugin) reader);

		}
	}

	/**
	 * Gets the single instance of SEProxyService.
	 *
	 * @return single instance of SEProxyService
	 */
	public static SEProxyService getInstance() {
		return uniqueInstance;
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * Sets the plugins.
	 *
	 * @param plugins
	 *            the new plugins
	 */
	public void setPlugins(List<ReadersPlugin> plugins) {
		this.plugins = plugins;
	}

	/**
	 * Gets the plugins.
	 *
	 * @return the plugins the list of interfaced reader’s plugins.
	 */
	public List<ReadersPlugin> getPlugins() {
		return plugins;
	}

}