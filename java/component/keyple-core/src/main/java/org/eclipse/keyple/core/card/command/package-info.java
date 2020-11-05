/**
 * Contains the necessary APIs for handling of card Commands.
 *
 * <p>It contains Command builders and response parser
 *
 * <p>The API for observing the plugins is defined by the interface CardCommand {@link
 * org.eclipse.keyple.core.card.command.CardCommand} {@link
 * org.eclipse.keyple.core.card.command.AbstractApduCommandBuilder} {@link
 * org.eclipse.keyple.core.card.command.AbstractIso7816CommandBuilder} {@link
 * org.eclipse.keyple.core.card.command.AbstractApduResponseParser}
 *
 * <p>{@link org.eclipse.keyple.core.card.command.exception.KeypleCardCommandException} {@link
 * org.eclipse.keyple.core.card.command.exception.KeypleCardCommandUnknownStatusException}
 *
 * <p>Contains the necessary APIs for observing plugins and readers (used by ticketing
 * applications).
 *
 * <p>It mainly contains the interfaces of the observable elements and the associated transport
 * POJOs.
 *
 * <p>The API for observing the plugins is defined by the interface {@link
 * org.eclipse.keyple.core.service.event.ObservablePlugin ObservablePlugin} and generates events
 * {@link org.eclipse.keyple.core.service.event.PluginEvent PluginEvent}.
 *
 * <p>The API for observing the readers is defined by the interface {@link
 * org.eclipse.keyple.core.service.event.ObservableReader ObservableReader} and generates events
 * {@link org.eclipse.keyple.core.service.event.ReaderEvent ReaderEvent}.<br>
 * The implementation of the abstract classes {@link
 * org.eclipse.keyple.core.service.event.AbstractDefaultSelectionsRequest
 * AbstractDefaultSelectionsRequest} and {@link
 * org.eclipse.keyple.core.service.event.AbstractDefaultSelectionsResponse
 * AbstractDefaultSelectionsResponse} is provided by Keyple in the package {@link
 * org.eclipse.keyple.core.card.message}.
 *
 * @since 0.9
 */
package org.eclipse.keyple.core.card.command;
