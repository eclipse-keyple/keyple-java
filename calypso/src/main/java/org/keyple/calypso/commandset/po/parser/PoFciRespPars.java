package org.keyple.calypso.commandset.po.parser;

import java.util.List;

import org.keyple.calypso.commandset.ApduResponseParser;
import org.keyple.calypso.commandset.ResponseUtils;
import org.keyple.calypso.commandset.enumTagUtils;
import org.keyple.calypso.commandset.dto.AID;
import org.keyple.calypso.commandset.dto.EF;
import org.keyple.calypso.commandset.dto.FCI;
import org.keyple.calypso.utils.LogUtils;
import org.keyple.seproxy.APDUResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides status code properties and the getters to access to the
 * structured fields of data from response of a Get Data response.
 *
 * @author Ixxi
 */
public class PoFciRespPars extends ApduResponseParser {

    /** The fci. */
    private FCI fci;

    /** The aid. */
    private AID aid;

    /** The ef. */
    private List<EF> ef;


    /**
     * Instantiates a new PoFciRespPars.
     *
     * @param response
     *            the response from Get Data APDU commmand
     * @param dataType
     *            the type of data to parse
     */
    public PoFciRespPars(APDUResponse response, enumTagUtils dataType) {
        super(response);
        logger = LoggerFactory.getLogger(this.getClass());
        logger.info("status : " + LogUtils.hexaToString(response.getStatusCode()));
        initStatusTable();
        logger.info("isSuccessful " +  isSuccessful());
        if (isSuccessful()) {
            logger.info(""+dataType);
            switch (dataType) {
            case FCI_TEMPLATE:
                fci = ResponseUtils.toFCI(response.getbytes());

                break;
            case AID_OF_CURRENT_DF_D:
                aid = ResponseUtils.toAID(response.getbytes());
                break;
            case LIST_OF_EF:
                ef = ResponseUtils.toEFList(response.getbytes());
                break;
            default:
                break;
            }
        }
        logger.debug(" status " + this.getStatusInformation());
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

    /**
     * Gets the fci.
     *
     * @return the fci
     */
    public FCI getFci() {
        return fci;
    }

    /**
     * Gets the aid.
     *
     * @return the aid
     */
    public AID getAid() {
        return aid;
    }

    /**
     * Gets the ef.
     *
     * @return the ef
     */
    public List<EF> getEf() {
        return ef;
    }

}
