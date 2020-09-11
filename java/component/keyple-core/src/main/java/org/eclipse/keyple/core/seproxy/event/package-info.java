/**
 * Contains the necessary APIs for observing plugins and readers (used by ticketing applications).
 *
 * <p>It mainly contains the interfaces of the observable elements and the associated transport
 * POJOs.
 *
 * <p>The API for observing the plugins is defined by the interface {@link
 * org.eclipse.keyple.core.seproxy.event.ObservablePlugin ObservablePlugin} and generates events
 * {@link org.eclipse.keyple.core.seproxy.event.PluginEvent PluginEvent}.
 *
 * <p>The API for observing the readers is defined by the interface {@link
 * org.eclipse.keyple.core.seproxy.event.ObservableReader ObservableReader} and generates events
 * {@link org.eclipse.keyple.core.seproxy.event.ReaderEvent ReaderEvent}.<br>
 * The implementation of the abstract classes {@link
 * org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest
 * AbstractDefaultSelectionsRequest} and {@link
 * org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse
 * AbstractDefaultSelectionsResponse} is provided by Keyple in the package {@link
 * org.eclipse.keyple.core.seproxy.message}.
 *
 * @since 0.9
 */
package org.eclipse.keyple.core.seproxy.event;
