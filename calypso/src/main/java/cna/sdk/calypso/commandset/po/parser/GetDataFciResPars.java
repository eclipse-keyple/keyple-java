package cna.sdk.calypso.commandset.po.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.ResponseUtils;
import cna.sdk.calypso.commandset.enumTagUtils;
import cna.sdk.calypso.commandset.dto.FCI;
import cna.sdk.seproxy.APDUResponse;

/**
 * This class provides status code properties and the getters to access to the
 * structured fields of data from response of a Get Data response.
 *
 * @author Ixxi
 */
public class GetDataFciResPars extends ApduResponseParser {

    /** The fci. */
    private FCI fci;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Instantiates a new PoFciRespPars.
     *
     * @param response
     *            the response from Get Data APDU commmand
     * @param dataType
     *            the type of data to parse
     */
    public GetDataFciResPars(APDUResponse response, enumTagUtils dataType) {
        super(response);
        initStatusTable();
        if (isSuccessful()) {

            fci = ResponseUtils.toFCI(response.getbytes());
        }
        logger.debug(this.getStatusInformation());
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] { (byte) 0x6A, (byte) 0x88 },
                new StatusProperties(false, "Data object not found (optional mode not available)."));
        statusTable.put(new byte[] { (byte) 0x6B, (byte) 0x00 }, new StatusProperties(false,
                "P1 or P2 value not supported (<>004fh, 0062h, 006Fh, 00C0h, 00D0h, 0185h and 5F52h, according to availabl optional modes)."));
        statusTable.put(new byte[] { (byte) 0x62, (byte) 0x83 },
                new StatusProperties(true, "Successful execution, FCI request and DF is invalidated."));
        statusTable.put(new byte[] { (byte) 0x90, (byte) 0x00 }, new StatusProperties(true, "Successful execution."));
    }

    byte[] getDfName() {
        if (fci != null) {
            return fci.getAid().getValue();
        }
        return null;
    }

    byte[] getApplicationSerialNumber() {
        if (fci != null) {
            return fci.getApplicationSN();
        }
        return null;
    }

    byte getBufferSizebyte() {
        if (fci != null) {
            return fci.getStartupInformation().getBufferSize();
        }
        return 0x00;
    }

    Integer getBufferSizeValue() {
        if (fci != null) {

        }
        return null;
    }

    byte getPlatformbyte() {
        if (fci != null) {
            return fci.getStartupInformation().getPlatform();
        }
        return 0x00;
    }

    byte getApplicationTypebyte() {
        if (fci != null) {
            return fci.getStartupInformation().getApplicationType();
        }
        return 0x00;
    }

    boolean isRev3Compliant() {
        if (fci != null) {

        }
        return false;
    }

    boolean isRev3_2ModeAvailable() {
        if (fci != null) {
            return fci.getStartupInformation().hasCalypsoRev32modeAvailable();
        }
        return false;
    }

    boolean isRatificationCommandRequired() {
        if (fci != null) {
            return fci.getStartupInformation().hasRatificationCommandRequired();
        }
        return false;
    }

    boolean hasCalypsoStoredValue() {
        if (fci != null) {
            return fci.getStartupInformation().hasCalypsoStoreValue();
        }
        return false;
    }

    boolean hasCalypsoPin() {
        if (fci != null) {
            return fci.getStartupInformation().hasCalypsoPin();
        }
        return false;
    }

    byte getApplicationSubtypebyte() {
        if (fci != null) {
            return fci.getStartupInformation().getApplicationSubtype();
        }
        return 0x00;
    }

    byte getSoftwareIssuerbyte() {
        if (fci != null) {
            return fci.getStartupInformation().getSoftwareIssuer();
        }
        return 0x00;
    }

    byte getSoftwareVersionbyte() {
        if (fci != null) {
            return fci.getStartupInformation().getSoftwareVersion();
        }
        return 0x00;
    }

    byte getSoftwareRevisionbyte() {
        if (fci != null) {
            return fci.getStartupInformation().getSoftwareRevision();
        }
        return 0x00;
    }

    boolean isDfInvalidated() {
        if (fci != null) {

        }
        return false;
    }
}
