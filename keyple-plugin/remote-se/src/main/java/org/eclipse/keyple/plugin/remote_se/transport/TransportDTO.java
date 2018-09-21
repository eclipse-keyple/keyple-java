package org.eclipse.keyple.plugin.remote_se.transport;


/**
 * Message with layer transport information
 */
public interface TransportDTO {


    /**
     * Retrieve the embedded Keyple DTO
     * @return embedded Keyple DTO
     */
    KeypleDTO getKeypleDTO();

    /**
     * Embed a Keyple DTO into a new TransportDTO with transport information
     * @param kdto : keyple DTO to be embedded
     * @return Transport DTO with embedded keyple DTO
     */
    TransportDTO nextTransportDTO(KeypleDTO kdto);


    //@Deprecated
    //DtoSender getDtoSender();



}
