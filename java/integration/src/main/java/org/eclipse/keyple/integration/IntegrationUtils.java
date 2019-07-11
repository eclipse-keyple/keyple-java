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
package org.eclipse.keyple.integration;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.keyple.calypso.command.po.parser.SelectFileRespPars;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.builder.security.UnlockCmdBuild;
import org.eclipse.keyple.calypso.transaction.SamResource;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import com.google.gson.*;

public class IntegrationUtils {

    public final static String PO_READER_NAME_REGEX = ".*(ASK|ACS).*";
    public final static String SAM_READER_NAME_REGEX =
            ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";

    /**
     * Get the terminals with names that match the expected pattern
     *
     * @param seProxyService SE Proxy service
     * @param pattern Pattern
     * @return SeReader
     * @throws KeypleReaderException Any error with the card communication
     */
    public static SeReader getReader(SeProxyService seProxyService, String pattern)
            throws KeypleReaderException {
        Pattern p = Pattern.compile(pattern);
        for (ReaderPlugin plugin : seProxyService.getPlugins()) {
            for (SeReader reader : plugin.getReaders()) {
                if (p.matcher(reader.getName()).matches()) {
                    return reader;
                }
            }
        }
        return null;
    }


    public static long bytesToLong(byte[] b, int length) {
        long result = 0;
        for (int i = 0; i < length; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }


    public static String getEfTypeName(String inEfType, boolean longModeFlag) {
        int efType = Integer.valueOf(inEfType, 16);

        return getEfTypeName(efType, longModeFlag);
    }


    public static String getEfTypeName(int inEfType, boolean longModeFlag) {

        switch (inEfType) {
            case SelectFileRespPars.EF_TYPE_BINARY: {
                return ((longModeFlag == true) ? "Binary" : "Bin ");
            }

            case SelectFileRespPars.EF_TYPE_LINEAR: {
                return ((longModeFlag == true) ? "Linear" : "Lin ");
            }

            case SelectFileRespPars.EF_TYPE_CYCLIC: {
                return ((longModeFlag == true) ? "Cyclic" : "Cycl");
            }

            case SelectFileRespPars.EF_TYPE_SIMULATED_COUNTERS: {
                return ((longModeFlag == true) ? "SimulatedCounter" : "SimC");
            }

            case SelectFileRespPars.EF_TYPE_COUNTERS: {
                return ((longModeFlag == true) ? "Counter" : "Cnt ");
            }
        }

        return "--";
    }


    public static String getAcName(String inAcValue, String inKeyLevel, boolean longModeFlag) {
        int acValue = Integer.valueOf(inAcValue, 16);
        int keyLevel = Integer.valueOf(inKeyLevel, 16);

        return getAcName(acValue, keyLevel, longModeFlag);
    }


    public static String getAcName(int inAcValue, int inKeyLevel, boolean longModeFlag) {


        switch (inAcValue) {

            case 0x1F: {
                return ((longModeFlag == true) ? "Always" : "AA");
            }

            case 0x00: {
                return ((longModeFlag == true) ? "Never" : "NN");
            }

            case 0x10: {
                return ((longModeFlag == true) ? ("Session" + inKeyLevel) : ("S" + inKeyLevel));
            }

            case 0x01: {
                return ((longModeFlag == true) ? "PIN" : "PN");
            }
        }

        return "--";
    }


    public static String getIssuerName(byte inIssuer) {

        switch (inIssuer) {

            case 0x00: {
                return "Paragon Id";
            }

            case 0x01: {
                return "Intec";
            }

            case 0x02: {
                return "Calypso";
            }

            case 0x04: {
                return "Thales";
            }

            case 0x05:
            case 0x0A: {
                return "Idemia";
            }

            case 0x06: {
                return "Axalto";
            }

            case 0x07: {
                return "Bull";
            }

            case 0x08: {
                return "Spirtech";
            }

            case 0x09: {
                return "BMS";
            }

            case 0x0B: {
                return "Gemplus";
            }

            case 0x0C: {
                return "Magnadata";
            }

            case 0x0D: {
                return "Calmell";
            }

            case 0x0E: {
                return "Mecstar";
            }

            case 0x0F: {
                return "ACG Identification";
            }

            case 0x10: {
                return "STMicroelectronics";
            }

            case 0x11: {
                return "CNA";
            }

            case 0x12: {
                return "G&D";
            }

            case 0x13: {
                return "OTI";
            }

            case 0x14: {
                return "Gemalto";
            }

            case 0x15: {
                return "Watchdata";
            }

            case 0x16: {
                return "Alios";
            }

            case 0x17: {
                return "S-P-S";
            }

            case 0x18: {
                return "ISRA";
            }

            case 0x19: {
                return "Trust Electronics";
            }

            case 0x1A: {
                return "Trusted Labs";
            }

            case 0x1B: {
                return "Neowave";
            }

            case 0x1C: {
                return "Digital People";
            }

            case 0x1D: {
                return "ABNote Europe";
            }

            case 0x1E: {
                return "Twinlinx";
            }

            case 0x1F: {
                return "Inteligensa";
            }

            case 0x20: {
                return "CNA";
            }

            case 0x21: {
                return "Innovatron";
            }

            case 0x22: {
                return "Austria Card";
            }

            case 0x23: {
                return "Carta+";
            }

            case 0x24: {
                return "Impimerie Nationale";
            }

            case 0x25:
            case 0x29: {
                return "HID Global";
            }

            case 0x26: {
                return "Card Project";
            }

            case 0x27: {
                return "PosteMobile";
            }

            case 0x28: {
                return "HB Technologies";
            }

            case 0x2A: {
                return "ANY Security Printing";
            }

            case 0x2B: {
                return "SELP";
            }

            case 0x2C: {
                return "Future Card";
            }

            case 0x2D: {
                return "iQuantics";
            }

            case 0x2E: {
                return "Calypso";
            }

            case 0x2F: {
                return "Aruba PEC";
            }
        }

        return "--";
    }


    public static class HexTypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

        public byte[] deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            return ByteArrayUtil.fromHex(json.getAsString());
        }

        public JsonElement serialize(byte[] data, Type typeOfSrc,
                JsonSerializationContext context) {
            return new JsonPrimitive(ByteArrayUtil.toHex(data));
        }
    }


    public static boolean unlockSam(SamResource samResource, byte[] unlockData)
            throws KeypleReaderException {
        // create an apdu requests list to handle SAM command
        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();

        // get the challenge from the PO
        apduRequests.add(new UnlockCmdBuild(SamRevision.C1, unlockData).getApduRequest());

        SeRequest seRequest = new SeRequest(apduRequests, ChannelState.KEEP_OPEN);

        SeResponse seResponse = ((ProxyReader) samResource.getSeReader()).transmit(seRequest);

        if (seResponse == null) {
            throw new IllegalStateException("Unlock SAM command command failed. Null response");
        }

        return seResponse.getApduResponses().get(0).isSuccessful();
    }


}
