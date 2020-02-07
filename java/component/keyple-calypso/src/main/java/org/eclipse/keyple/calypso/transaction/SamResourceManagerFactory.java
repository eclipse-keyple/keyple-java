/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.transaction;

import static org.eclipse.keyple.calypso.command.sam.SamRevision.AUTO;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SamResourceManagerFactory {
    private static final Logger logger = LoggerFactory.getLogger(SamResourceManagerFactory.class);

    /* the maximum time (in tenths of a second) during which the BLOCKING mode will wait */
    protected final static int MAX_BLOCKING_TIME = 1000; // 10 sec

    /**
     * Create a SAM resource from the provided SAM reader.
     * <p>
     * Proceed with the SAM selection and combine the SAM reader and the Calypso SAM resulting from
     * the selection.
     *
     * @param samReader the SAM reader with which the APDU exchanges will be done.
     * @return a {@link SamResource}
     * @throws KeypleReaderException if an reader error occurs while doing the selection
     */
    static protected SamResource createSamResource(SeReader samReader) throws KeypleReaderException {
        logger.trace("Create SAM resource from reader NAME = {}", samReader.getName());

        samReader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO7816_3, ".*");

        SeSelection samSelection = new SeSelection();

        SamSelector samSelector = new SamSelector(new SamIdentifier(AUTO, null, null), "SAM");

        /* Prepare selector, ignore MatchingSe here */
        samSelection.prepareSelection(new SamSelectionRequest(samSelector));

        SelectionsResult selectionsResult = samSelection.processExplicitSelection(samReader);
        if (!selectionsResult.hasActiveSelection()) {
            throw new IllegalStateException("Unable to open a logical channel for SAM!");
        }
        CalypsoSam calypsoSam = (CalypsoSam) selectionsResult.getActiveSelection().getMatchingSe();
        return new SamResource(samReader, calypsoSam);
    }

    /**
     * Instantiate a new SamResourceManager.
     * <p>
     * The samReaderPlugin is used to retrieve the available SAM according to the provided filter.
     * <p>
     * Setup a plugin observer if the reader plugin is observable.
     *
     * @param readerPlugin the plugin through which SAM readers are accessible
     * @param samReaderFilter the regular expression defining how to identify SAM readers among
     *        others.
     * @throws KeypleReaderException throw if an error occurs while getting the readers list.
     * @return SamResourceManager working with a default plugin
     */
    static public SamResourceManager instantiate(ReaderPlugin readerPlugin, String samReaderFilter) throws KeypleReaderException {
        return new SamResourceManagerDefault(readerPlugin, samReaderFilter);
    }

    /**
     * Instantiate a new SamResourceManager.
     * <p>
     * The samReaderPlugin is used to retrieve the available SAM in the ReaderPoolPlugin.
     * <p>
     * Setup a plugin observer if the reader plugin is observable.
     *
     * @param samReaderPoolPlugin the plugin through which SAM readers are accessible
     * @return SamResourceManager working with a pool plugin
     */
    static public SamResourceManager instantiate(ReaderPoolPlugin samReaderPoolPlugin) {
        return new SamResourceManagerPool(samReaderPoolPlugin);
    }

}
