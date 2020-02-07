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
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSamResourceManager {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSamResourceManager.class);

    protected final ReaderPlugin samReaderPlugin;

    public enum AllocationMode {
        BLOCKING, NON_BLOCKING
    }

    /* the maximum time (in tenths of a second) during which the BLOCKING mode will wait */
    protected final static int MAX_BLOCKING_TIME = 1000; // 10 sec

    AbstractSamResourceManager(ReaderPlugin samPlugin) {
        this.samReaderPlugin = samPlugin;
    }

    /**
     * Allocate a SAM resource from the specified SAM group.
     * <p>
     * In the case where the allocation mode is BLOCKING, this method will wait until a SAM resource
     * becomes free and then return the reference to the allocated resource. However, the BLOCKING
     * mode will wait a maximum time defined in tenths of a second by MAX_BLOCKING_TIME.
     * <p>
     * In the case where the allocation mode is NON_BLOCKING and no SAM resource is available, this
     * method will return null.
     * <p>
     * If the samGroup argument is null, the first available SAM resource will be selected and
     * returned regardless of its group.
     *
     * @param allocationMode the blocking/non-blocking mode
     * @param samIdentifier the targeted SAM identifier
     * @return a SAM resource
     * @throws KeypleReaderException if a reader error occurs
     */
    abstract public SamResource allocateSamResource(AllocationMode allocationMode,
            SamIdentifier samIdentifier) throws KeypleReaderException;

    /**
     * Free a previously allocated SAM resource.
     *
     * @param samResource the SAM resource reference to free
     */
    abstract public void freeSamResource(SamResource samResource);

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
    protected SamResource createSamResource(SeReader samReader) throws KeypleReaderException {
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



}
