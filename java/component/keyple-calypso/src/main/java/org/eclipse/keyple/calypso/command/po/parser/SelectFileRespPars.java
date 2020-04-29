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
package org.eclipse.keyple.calypso.command.po.parser;

import static org.eclipse.keyple.core.util.bertlv.Tag.TagType.PRIMITIVE;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.builder.SelectFileCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoDataAccessException;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoIllegalParameterException;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.bertlv.TLV;
import org.eclipse.keyple.core.util.bertlv.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides status code properties and the getters to access to the structured fields of
 * data from response to a Select File command (available from the parent class).
 * <p>
 * The value of the Proprietary Information tag is extracted from the Select File response and made
 * available using the corresponding getter.
 */
public final class SelectFileRespPars extends AbstractPoResponseParser {
    private static final Logger logger = LoggerFactory.getLogger(SelectFileRespPars.class);
    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6700, new StatusProperties("Lc value not supported.",
                CalypsoPoIllegalParameterException.class));
        m.put(0x6A82, new StatusProperties("File not found.", CalypsoPoDataAccessException.class));
        m.put(0x6119, new StatusProperties("Correct execution (ISO7816 T=0).", null));
        STATUS_TABLE = m;
    }

    private byte[] proprietaryInformation;

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    /* Proprietary Information: context-specific class, primitive, tag number 5h => tag field 85h */
    private static final Tag TAG_PROPRIETARY_INFORMATION = new Tag(0x05, Tag.CONTEXT, PRIMITIVE);

    /**
     * Instantiates a new SelectFileRespPars.
     * 
     * @param response the response from the PO
     * @param builder the reference to the builder that created this parser
     */
    public SelectFileRespPars(ApduResponse response, SelectFileCmdBuild builder) {
        super(response, builder);
        proprietaryInformation = null;
    }

    /**
     * @return the content of the proprietary information tag present in the response to the Select
     *         File command
     */
    public byte[] getProprietaryInformation() {
        if (proprietaryInformation == null) {
            TLV tlv = new TLV(response.getDataOut());
            if (!tlv.parse(TAG_PROPRIETARY_INFORMATION, 0)) {
                throw new IllegalStateException("Proprietary information: tag not found.");
            }
            proprietaryInformation = tlv.getValue();
            Assert.getInstance().isEqual(proprietaryInformation.length, 23,
                    "proprietaryInformation");
        }
        return proprietaryInformation;
    }
}
