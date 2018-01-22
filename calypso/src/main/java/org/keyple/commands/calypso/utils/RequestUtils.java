package org.keyple.commands.calypso.utils;

import java.util.ArrayList;
import java.util.List;

import org.keyple.commands.calypso.CalypsoCommands;
import org.keyple.commands.calypso.InconsistentCommandException;
import org.keyple.commands.calypso.dto.CalypsoRequest;
import org.keyple.seproxy.ApduRequest;

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
    public static ApduRequest constructAPDURequest(CalypsoRequest request) {
        return constructAPDURequest(request, 0);
    }

    public static void controlRequestConsistency(CalypsoCommands command, ApduRequest request)
            throws InconsistentCommandException {
        boolean isRequestInconsistent = true;
        if (request != null) {
            if (request.getbytes() != null) {
                if (request.getbytes().length >= 2) {
                    if (command.getInstructionByte() == request.getbytes()[1]) {
                        isRequestInconsistent = false;
                    }
                }
            }
        }
        if (isRequestInconsistent) {
            throw new InconsistentCommandException();
        }
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
    public static ApduRequest constructAPDURequest(CalypsoRequest request, int caseId) {

        int localCaseId = caseId;
        if (caseId == 0) {
            // try to retrieve case
            if (request.getDataIn().length == 0 && request.getLe() == 0x00) {
                localCaseId = 1;
            }
            if (request.getDataIn().length == 0 && request.getLe() != 0x00) {
                localCaseId = 2;
            }
            if (request.getDataIn().length != 0 && request.getLe() == 0x00) {
                localCaseId = 3;
            }
            if (request.getDataIn().length != 0 && request.getLe() != 0x00) {
                localCaseId = 4;
            }
        }

        List<Byte> tableaubytesAdpuRequest = new ArrayList<Byte>();

        tableaubytesAdpuRequest.add(request.getCla());
        tableaubytesAdpuRequest.add(request.getIns().getInstructionByte());
        tableaubytesAdpuRequest.add(request.getP1());
        tableaubytesAdpuRequest.add(request.getP2());

        if (request.getDataIn().length == 0 && request.isForceLc()) {
            tableaubytesAdpuRequest.add(request.getLc());
        }

        if (request.getDataIn().length != 0) {
            tableaubytesAdpuRequest.add(request.getLc());
            for(int v=0;v < ((int)request.getLc());v++){
                tableaubytesAdpuRequest.add(request.getDataIn()[v]);
            }
        }
        
        
        if (request.getLe() != 0x00 || request.isForceLe()) {
            if (localCaseId == 4) {
                tableaubytesAdpuRequest.add((byte)0x00);
            } else {
                if(request.getLe() != 0x00|| request.isForceLe()){
                    tableaubytesAdpuRequest.add(request.getLe());
                }
            }
        }

        byte[] command = new byte[tableaubytesAdpuRequest.size()];
        for (int i = 0; i < tableaubytesAdpuRequest.size(); i++) {
            command[i] = tableaubytesAdpuRequest.get(i);
        }
        ApduRequest apdu = new ApduRequest(command, localCaseId == 4);
        return apdu;
    }
}
