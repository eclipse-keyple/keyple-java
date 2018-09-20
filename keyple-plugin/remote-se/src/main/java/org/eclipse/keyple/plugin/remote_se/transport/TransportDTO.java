package org.eclipse.keyple.plugin.remote_se.transport;

/**
 * Message with layer transport information
 */
public interface TransportDTO {

    /*
    Get information about keyple DTO
     */
    KeypleDTO getKeypleDTO();

    /*
    Build next message to be sent with layer transport information
     */
    TransportDTO nextTransportDTO(KeypleDTO kdto);


    DtoSender getDtoSender();


}
