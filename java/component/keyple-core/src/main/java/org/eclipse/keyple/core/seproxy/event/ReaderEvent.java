/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.seproxy.event;

import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsResponse;

/**
 * This POJO is used to propagate a change of a card state in an {@link ObservableReader}.
 *
 * <p>The various events that can occur concern the insertion and removal of a card from a reader.
 *
 * <p>When an insertion is made there are two cases depending on whether a default selection has
 * been programmed or not.
 *
 * @since 0.9
 */
public final class ReaderEvent {

  private final String pluginName;
  private final String readerName;
  private final DefaultSelectionsResponse defaultResponses;

  /**
   * The different types of reader events, reflecting the status of the reader regarding the
   * presence of a Secure Element.
   *
   * @since 0.9
   */
  public enum EventType {

    /** An timeout error occurred. */
    TIMEOUT_ERROR,

    /** A card has been inserted. */
    SE_INSERTED,

    /** A card has been inserted and the default requests process has been successfully operated. */
    SE_MATCHED,

    /** The card has been removed and is no longer able to communicate with the reader */
    SE_REMOVED
  }

  /** The type of event */
  private final EventType eventType;

  /**
   * ReaderEvent constructor for simple insertion notification mode
   *
   * @param pluginName the name of the current plugin (should be not null)
   * @param readerName the name of the current reader (should be not null)
   * @param eventType the type of event (should be not null)
   * @param defaultSelectionsResponse the response to the default AbstractDefaultSelectionsRequest
   *     (may be null)
   * @since 0.9
   */
  public ReaderEvent(
      String pluginName,
      String readerName,
      EventType eventType,
      AbstractDefaultSelectionsResponse defaultSelectionsResponse) {
    this.pluginName = pluginName;
    this.readerName = readerName;
    this.eventType = eventType;
    this.defaultResponses = (DefaultSelectionsResponse) defaultSelectionsResponse;
  }

  /**
   * Gets the name of the plugin from which the reader that generated the event comes from
   *
   * @return A not empty string.
   * @since 0.9
   */
  public String getPluginName() {
    return pluginName;
  }

  /**
   * Gets the name of the reader that generated the event comes from
   *
   * @return A not empty string.
   * @since 0.9
   */
  public String getReaderName() {
    return readerName;
  }

  /**
   * Gets the reader event type.
   *
   * @return A not null value.
   * @since 0.9
   */
  public EventType getEventType() {
    return eventType;
  }

  /**
   * Gets the default selection response that may be present when the event is {@link
   * EventType#SE_INSERTED}, always present when the event is {@link EventType#SE_MATCHED} and null
   * in the others cases.
   *
   * @return A nullable value.
   * @since 0.9
   */
  public AbstractDefaultSelectionsResponse getDefaultSelectionsResponse() {
    return defaultResponses;
  }

  /**
   * Gets the {@link ReaderPlugin} from which the reader that generated the event comes from.
   *
   * @return A not null reference.
   * @since 0.9
   */
  public ReaderPlugin getPlugin() {
    return SeProxyService.getInstance().getPlugin(pluginName);
  }

  /**
   * Gets the {@link SeReader} from which generated event comes from
   *
   * @return A not null reference.
   * @since 0.9
   */
  public SeReader getReader() {
    return getPlugin().getReader(readerName);
  }
}
