/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.example.remote.application;

import static org.eclipse.keyple.calypso.transaction.PoSelector.*;

import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.SamResourceManager;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.plugin.reader.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.common.calypso.postructure.CalypsoClassicInfo;
import org.eclipse.keyple.plugin.remotese.pluginse.MasterAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Configure the Virtual Reader on READER_CONNECTED. */
public class RemoteSePluginObserver implements ObservablePlugin.PluginObserver {

  private static final Logger logger = LoggerFactory.getLogger(RemoteSePluginObserver.class);

  private final String nodeId;
  private final MasterAPI masterAPI;
  private final SamResourceManager samResourceManager;

  RemoteSePluginObserver(
      MasterAPI masterAPI, SamResourceManager samResourceManager, String nodeId) {
    this.nodeId = nodeId;
    this.masterAPI = masterAPI;
    this.samResourceManager = samResourceManager;
  }

  @Override
  public void update(PluginEvent event) {
    logger.info(
        "{} event {} {} {}",
        nodeId,
        event.getEventType(),
        event.getPluginName(),
        event.getReaderNames().first());
    /*
     * Process events
     */
    switch (event.getEventType()) {
      case READER_CONNECTED:
        /** a new virtual reader is connected, let's configure it */
        try {
          ReaderPlugin remoteSEPlugin =
              SeProxyService.getInstance().getPlugin(event.getPluginName());

          Reader poReader = remoteSEPlugin.getReader(event.getReaderNames().first());

          logger.info("{} Configure SeSelection", nodeId);

          /* set default selection request */
          final SeSelection seSelection = new SeSelection();

          /*
           * Setting of an AID based selection of a Calypso REV3 PO
           *
           * Select the first application matching the selection AID whatever the card
           * communication protocol keep the logical channel open after the selection
           *
           * Calypso selection: configures a PoSelectionRequest with all the desired
           * attributes to make the selection and read additional information afterwards
           */
          PoSelectionRequest poSelectionRequest =
              new PoSelectionRequest(
                  PoSelector.builder()
                      .seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                      .aidSelector(
                          AidSelector.builder().aidToSelect(CalypsoClassicInfo.AID).build())
                      .invalidatedPo(InvalidatedPo.ACCEPT)
                      .build());

          logger.info("{} Create a PoSelectionRequest", nodeId);

          /*
           * Add the selection case to the current selection (we could have added other
           * cases here)
           */
          seSelection.prepareSelection(poSelectionRequest);

          logger.info("{} setDefaultSelectionRequest for PoReader {}", nodeId, poReader.getName());

          /*
           * Provide the Reader with the selection operation to be processed when a PO
           * is inserted.
           */
          ((ObservableReader) poReader)
              .setDefaultSelectionRequest(
                  seSelection.getSelectionOperation(),
                  ObservableReader.NotificationMode.MATCHED_ONLY);

          // observe reader events
          logger.info(
              "{} Create a new Po Observer for the Virtual Reader {}", nodeId, poReader.getName());

          ((ObservableReader) poReader)
              .addObserver(
                  new PoVirtualReaderObserver(masterAPI, samResourceManager, seSelection, nodeId));

        } catch (KeypleReaderNotFoundException e) {
          logger.error(e.getMessage());
          e.printStackTrace();
        } catch (KeyplePluginNotFoundException e) {
          logger.error(e.getMessage());
          e.printStackTrace();
        }
        break;
      case READER_DISCONNECTED:
        /*
         * Virtual reader has been disconnected
         */
        logger.info(
            "{} READER_DISCONNECTED {} {}",
            nodeId,
            event.getPluginName(),
            event.getReaderNames().first());
        break;
    }
  }
}
