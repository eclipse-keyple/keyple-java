/**
 * Contains the necessary APIs for observing plugins and readers (used by ticketing applications).
 *
 * <p>It mainly contains the interfaces of the observable elements and the associated transport
 * POJOs.
 *
 * <p>The API for observing the plugins is defined by the interface {@link
 * org.eclipse.keyple.core.reader.event.ObservablePlugin ObservablePlugin} and generates events
 * {@link org.eclipse.keyple.core.reader.event.PluginEvent PluginEvent}.
 *
 * <p>The API for observing the readers is defined by the interface {@link
 * org.eclipse.keyple.core.reader.event.ObservableReader ObservableReader} and generates events
 * {@link org.eclipse.keyple.core.reader.event.ReaderEvent ReaderEvent}.<br>
 * The implementation of the abstract POJO classes {@link
 * org.eclipse.keyple.core.reader.event.AbstractDefaultSelectionsRequest
 * AbstractDefaultSelectionsRequest} and {@link
 * org.eclipse.keyple.core.reader.event.AbstractDefaultSelectionsResponse
 * AbstractDefaultSelectionsResponse} is provided by Keyple in the package {@link
 * org.eclipse.keyple.core.reader.message}.
 *
 * @since 0.9
 */
package org.eclipse.keyple.core.reader.event;
