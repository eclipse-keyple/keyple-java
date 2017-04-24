package cna.sdk.calypso.commandset.po.parser;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.ResponseUtils;
import cna.sdk.calypso.commandset.enumTagUtils;
import cna.sdk.calypso.commandset.dto.AID;
import cna.sdk.calypso.commandset.dto.EF;
import cna.sdk.calypso.commandset.dto.FCI;
import cna.sdk.seproxy.APDUResponse;


/**This class provides status code properties and the getters to
 *         access to the structured fields of data from response of a Get
 *         Data response.
 * @author Ixxi
 */
public class PoFciRespPars extends ApduResponseParser {

	/** The fci. */
    private FCI fci;

	/** The aid. */
    private AID aid;

	/** The ef. */
    private List<EF> ef;

    Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Instantiates a new PoFciRespPars.
	 *
	 * @param response the response from Get Data APDU commmand
	 * @param dataType the type of data to parse
	 */
	public PoFciRespPars(APDUResponse response, enumTagUtils dataType) {
        super(response);
        initStatusTable();
        if (isSuccessful()) {
			switch (dataType) {
            case FCI_TEMPLATE:
                fci = ResponseUtils.toFCI(response.getbytes());

                break;
            case AID_OF_CURRENT_DF:
                aid = ResponseUtils.toAID(response.getbytes());
                break;
            case LIST_OF_EF:
                ef = ResponseUtils.toEFList(response.getbytes());
                break;
            default:
                break;
            }
        }
        logger.debug(this.getStatusInformation());
    }

	/**
	 * Initializes the status table.
	 */
    private void initStatusTable() {
        statusTable.put(new byte[]{(byte) 0x6A, (byte) 0x88},
                new StatusProperties(false, "Data object not found (optional mode not available)."));
        statusTable.put(new byte[]{(byte) 0x6B, (byte) 0x00}, new StatusProperties(false,
                "P1 or P2 value not supported (<>004fh, 0062h, 006Fh, 00C0h, 00D0h, 0185h and 5F52h, according to availabl optional modes)."));
        statusTable.put(new byte[]{(byte) 0x62, (byte) 0x83},
                new StatusProperties(true, "Successful execution, FCI request and DF is invalidated."));
        statusTable.put(new byte[] {(byte) 0x90, (byte) 0x00}, new StatusProperties(true, "Successful execution."));
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
