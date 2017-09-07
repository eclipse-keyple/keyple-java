package org.keyple.calypso.commandset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.keyple.calypso.commandset.dto.CalypsoRequest;
import org.keyple.seproxy.APDURequest;

/**
 * This class eases the construction of APDURequest.
 *
 * @author Ixxi
 */
public class RequestUtils {

	
    private RequestUtils() {
	}

	/**
     * Construct APDU request.
     *
     * @param request
     *            the request
     * @return the APDU request
     */
    public static APDURequest constructAPDURequest(CalypsoRequest request) {
        return constructAPDURequest(request, 0);
    }

    /**
     * Construct APDU request.
     *
     * @param request
     *            the request
     * @param caseId
     *            the case id
     * @return the APDU request
     */
    public static APDURequest constructAPDURequest(CalypsoRequest request, int caseId) {

        // Case IN / OUT
        // 1 : N / N
        // 2 : N / Y
        // 3 : Y / N
        // 4 : Y / Y
        int localCaseId = caseId;
        if (caseId == 0) {
            // try to retrieve case
            if (ArrayUtils.isEmpty(request.getDataIn()) && request.getLe() == 0x00) {
                localCaseId = 1;
            }
            if (ArrayUtils.isEmpty(request.getDataIn()) && request.getLe() != 0x00) {
                localCaseId = 2;
            }
            if (!ArrayUtils.isEmpty(request.getDataIn()) && request.getLe() == 0x00) {
                localCaseId = 3;
            }
            if (!ArrayUtils.isEmpty(request.getDataIn()) && request.getLe() != 0x00) {
                localCaseId = 4;
            }
        }

        List<Byte> tableaubytesAdpuRequest = new ArrayList<>();

        tableaubytesAdpuRequest.add(request.getCla());
        tableaubytesAdpuRequest.add(request.getIns().getInstructionbyte());
        tableaubytesAdpuRequest.add(request.getP1());
        tableaubytesAdpuRequest.add(request.getP2());

        if (!ArrayUtils.isEmpty(request.getDataIn())) {
        	tableaubytesAdpuRequest.add((byte) request.getDataIn().length);
        	tableaubytesAdpuRequest.addAll(Arrays.asList(ArrayUtils.toObject(request.getDataIn())));
        }
        if (request.getLe() != 0x00) {
        	tableaubytesAdpuRequest.add(request.getLe());
        }

        APDURequest apdu = new APDURequest(ArrayUtils.toPrimitive(tableaubytesAdpuRequest.toArray(new Byte[0])), localCaseId == 4);
//        apdu.setInstruction(request.getIns().getName());
        return apdu;
    }
}
