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
package org.eclipse.keyple.example.generic.pc.Demo_ObservableReaderNotification;

import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class ObservableReaderNotificationEngine {
  private static final Logger logger =
      LoggerFactory.getLogger(ObservableReaderNotificationEngine.class);

  private final SpecificPluginObserver pluginObserver;

  public ObservableReaderNotificationEngine() {
    /* initializes observers */
    SpecificReaderObserver readerObserver = new SpecificReaderObserver();
    pluginObserver = new SpecificPluginObserver(readerObserver);
  }

  public void setPluginObserver() {

    /*
     * We add an observer to each plugin (only one in this example) the readers observers will
     * be added dynamically upon plugin notification (see SpecificPluginObserver.update)
     */
    Collection<Plugin> plugins = SmartCardService.getInstance().getPlugins().values();
    for (Plugin plugin : plugins) {

      if (plugin instanceof ObservablePlugin) {
        /* start detection for all already present readers */
        for (Reader reader : plugin.getReaders().values()) {
          ((ObservableReader) reader).startCardDetection(ObservableReader.PollingMode.REPEATING);
        }
        logger.info("Add observer PLUGINNAME = {}", plugin.getName());
        ((ObservablePlugin) plugin).addObserver(this.pluginObserver);
      } else {
        logger.info("PLUGINNAME = {} isn't observable", plugin.getName());
      }
    }
  }

  /** This method is called whenever a Reader event occurs (card insertion/removal) */
  public class SpecificReaderObserver implements ObservableReader.ReaderObserver {

    SpecificReaderObserver() {
      super();
    }

    public void update(ReaderEvent event) {

      logger.info(
          "ReaderEvent: {} : {} : {}",
          event.getPluginName(),
          event.getReaderName(),
          event.getEventType());

      switch (event.getEventType()) {
        case CARD_MATCHED:
          /*
           * Informs the underlying layer of the end of the card processing, in order to
           * manage the removal sequence.
           */
          try {
            ((ObservableReader) (event.getReader())).finalizeCardProcessing();
          } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
          } catch (KeyplePluginNotFoundException e) {
            e.printStackTrace();
          }
          break;

        case CARD_INSERTED:
          /*
           * end of the card processing is automatically done
           */
          break;
      }

      /* just log the event */
      logger.info(
          "Event: PLUGINNAME = {}, READERNAME = {}, EVENT = {}",
          event.getPluginName(),
          event.getReaderName(),
          event.getEventType().name());
    }
  }

  /** This method is called whenever a Plugin event occurs (reader insertion/removal) */
  public class SpecificPluginObserver implements ObservablePlugin.PluginObserver {

    SpecificReaderObserver readerObserver;

    SpecificPluginObserver(SpecificReaderObserver readerObserver) {
      this.readerObserver = readerObserver;
    }

    @Override
    public void update(PluginEvent event) {
      for (String readerName : event.getReaderNames()) {
        Reader reader = null;
        logger.info(
            "PluginEvent: PLUGINNAME = {}, READERNAME = {}, EVENTTYPE = {}",
            event.getPluginName(),
            readerName,
            event.getEventType());

        if (event.getEventType() != PluginEvent.EventType.READER_DISCONNECTED) {
          /* We retrieve the reader object from its name. */
          try {
            reader =
                SmartCardService.getInstance()
                    .getPlugin(event.getPluginName())
                    .getReader(readerName);
          } catch (KeyplePluginNotFoundException e) {
            e.printStackTrace();
          } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
          }
        }
        switch (event.getEventType()) {
          case READER_CONNECTED:
            logger.info("New reader! READERNAME = {}", reader.getName());

            /*
             * We are informed here of a disconnection of a reader.
             *
             * We add an observer to this reader if this is possible.
             */
            if (reader instanceof ObservableReader) {
              if (readerObserver != null) {
                logger.info("Add observer READERNAME = {}", reader.getName());
                ((ObservableReader) reader).addObserver(readerObserver);
                ((ObservableReader) reader)
                    .startCardDetection(ObservableReader.PollingMode.REPEATING);
              } else {
                logger.info("No observer to add READERNAME = {}", reader.getName());
              }
            }
            break;
          case READER_DISCONNECTED:
            /*
             * We are informed here of a disconnection of a reader.
             *
             * The reader object still exists but will be removed from the reader list
             * right after. Thus, we can properly remove the observer attached to this
             * reader before the list update.
             */
            logger.info("Reader removed. READERNAME = {}", readerName);
            break;
          default:
            logger.info("Unexpected reader event. EVENT = {}", event.getEventType().name());
            break;
        }
      }
    }
  }
}
