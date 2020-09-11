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
 * A {@link ReaderEvent} is used to propagate a change of a SE state in {@link ObservableReader}.
 *
 * <p>The various events that can occur concern the insertion and removal of an SE from a reader.
 *
 * <p>When an insertion is made there are two cases depending on whether a default selection has
 * been programmed or not.
 *
 * @since 0.9
 */
public final class ReaderEvent {

  /** The name of the plugin handling the reader that produced the event */
  private final String pluginName;

  /** The name of the reader that produced the event */
  private final String readerName;

  /**
   * The response to the selection request Note: although the object is instantiated externally, we
   * use DefaultSelectionsResponse here to keep ReaderEvent serializable
   */
  private final DefaultSelectionsResponse defaultResponses;

  /**
   * The different types of reader events, reflecting the status of the reader regarding the
   * presence of a Secure Element
   *
   * @since 0.9
   */
  public enum EventType {

    /** An timeout error occurred. */
    TIMEOUT_ERROR,

    /** A SE has been inserted. */
    SE_INSERTED,

    /** A SE has been inserted and the default requests process has been successfully operated. */
    SE_MATCHED,

    /** The SE has been removed and is no longer able to communicate with the reader */
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
   * @return A string containing a plugin name
   * @since 0.9
   */
  public String getPluginName() {
    return pluginName;
  }

  /**
   * Gets the name of the reader that generated the event comes from
   *
   * @return A string containing a reader name
   * @since 0.9
   */
  public String getReaderName() {
    return readerName;
  }

  /**
   * Gets the type of {@link ReaderEvent}
   *
   * @return A enum constant of type {@link EventType}
   * @since 0.9
   */
  public EventType getEventType() {
    return eventType;
  }

  /**
   * Gets the default selection response that may be present when the event is {@link
   * EventType#SE_INSERTED} or {@link EventType#SE_MATCHED}
   *
   * @return the default selection response
   * @since 0.9
   */
  public AbstractDefaultSelectionsResponse getDefaultSelectionsResponse() {
    return defaultResponses;
  }

  /**
   * Gets the plugin from which the reader that generated the event comes from
   *
   * @return The {@link ReaderPlugin} involved in this event
   * @since 0.9
   */
  public ReaderPlugin getPlugin() {
    return SeProxyService.getInstance().getPlugin(pluginName);
  }

  /**
   * Gets the reader that generated the event comes from
   *
   * @return The {@link SeReader} involved in this event
   * @since 0.9
   */
  public SeReader getReader() {
    return getPlugin().getReader(readerName);
  }
}
