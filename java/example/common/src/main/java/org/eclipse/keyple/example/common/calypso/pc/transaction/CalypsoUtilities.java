/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.common.calypso.pc.transaction;

import static org.eclipse.keyple.calypso.command.sam.SamRevision.C1;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionSetting.AccessLevel;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.PoSecuritySettings;
import org.eclipse.keyple.calypso.transaction.SamResource;
import org.eclipse.keyple.calypso.transaction.SamSelectionRequest;
import org.eclipse.keyple.calypso.transaction.SamSelector;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.example.common.ReaderUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CalypsoUtilities {
    private static final Logger logger = LoggerFactory.getLogger(CalypsoUtilities.class);

    private static Properties properties;

    static {
        properties = new Properties();

        String propertiesFileName = "config.properties";

        InputStream inputStream =
                CalypsoUtilities.class.getClassLoader().getResourceAsStream(propertiesFileName);

        try {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException(
                        "property file '" + propertiesFileName + "' not found!");
            }
        } catch (FileNotFoundException e) {
            logger.error("File not found exception: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("IO exception: {}", e.getMessage());
        }
    }

    private CalypsoUtilities() {}

    /**
     * Get the default reader for PO communications
     * 
     * @return a SeReader object
     * @throws KeypleException if an error occurred
     */
    public static SeReader getDefaultPoReader() throws KeypleException {
        SeReader poReader =
                ReaderUtilities.getReaderByName(properties.getProperty("po.reader.regex"));

        ReaderUtilities.setContactlessSettings(poReader);

        return poReader;
    }

    /**
     * Get the default reader for SAM communications
     * 
     * @return a {@link SamResource} object
     * @throws KeypleException if an error occurred
     */
    public static SamResource getDefaultSamResource() throws KeypleException {
        SeReader samReader =
                ReaderUtilities.getReaderByName(properties.getProperty("sam.reader.regex"));

        ReaderUtilities.setContactsSettings(samReader);

        /*
         * Open logical channel for the SAM inserted in the reader
         *
         * (We expect the right is inserted)
         */
        return checkSamAndOpenChannel(samReader);
    }

    public static PoSecuritySettings getSecuritySettings(SamResource samResource) {

        // The default KIF values for personalization, loading and debiting
        final byte DEFAULT_KIF_PERSO = (byte) 0x21;
        final byte DEFAULT_KIF_LOAD = (byte) 0x27;
        final byte DEFAULT_KIF_DEBIT = (byte) 0x30;
        // The default key record number values for personalization, loading and debiting
        // The actual value should be adjusted.
        final byte DEFAULT_KEY_RECORD_NUMBER_PERSO = (byte) 0x01;
        final byte DEFAULT_KEY_RECORD_NUMBER_LOAD = (byte) 0x02;
        final byte DEFAULT_KEY_RECORD_NUMBER_DEBIT = (byte) 0x03;
        /* define the security parameters to provide when creating PoTransaction */
        return new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                .sessionDefaultKif(AccessLevel.SESSION_LVL_PERSO, DEFAULT_KIF_PERSO)//
                .sessionDefaultKif(AccessLevel.SESSION_LVL_LOAD, DEFAULT_KIF_LOAD)//
                .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)//
                .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_PERSO,
                        DEFAULT_KEY_RECORD_NUMBER_PERSO)//
                .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_LOAD,
                        DEFAULT_KEY_RECORD_NUMBER_LOAD)//
                .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                        DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                .build();
    }

    /**
     * Check SAM presence and consistency and return a SamResource when everything is correct.
     * <p>
     * Throw an exception if the expected SAM is not available
     *
     * @param samReader the SAM reader
     */
    public static SamResource checkSamAndOpenChannel(SeReader samReader) {
        /*
         * check the availability of the SAM doing a ATR based selection, open its physical and
         * logical channels and keep it open
         */
        SeSelection samSelection = new SeSelection();

        SamSelector samSelector =
                SamSelector.builder().seProtocol(SeCommonProtocols.PROTOCOL_ISO7816_3)
                        .samRevision(C1).serialNumber(".*").build();

        /* Prepare selector, ignore AbstractMatchingSe here */
        samSelection.prepareSelection(new SamSelectionRequest(samSelector));
        CalypsoSam calypsoSam;

        try {
            if (samReader.isSePresent()) {
                SelectionsResult selectionsResult =
                        samSelection.processExplicitSelection(samReader);
                if (selectionsResult.hasActiveSelection()) {
                    calypsoSam = (CalypsoSam) selectionsResult.getActiveMatchingSe();
                } else {
                    throw new IllegalStateException("Unable to open a logical channel for SAM!");
                }
            } else {
                throw new IllegalStateException(
                        "No SAM is present in the reader " + samReader.getName());
            }
        } catch (KeypleReaderException e) {
            throw new IllegalStateException("Reader exception: " + e.getMessage());
        } catch (KeypleException e) {
            throw new IllegalStateException("Reader exception: " + e.getMessage());
        }
        logger.info("The SAM resource has been created");
        return new SamResource(samReader, calypsoSam);
    }
}
