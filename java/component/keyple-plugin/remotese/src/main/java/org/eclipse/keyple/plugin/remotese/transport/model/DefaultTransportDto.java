package org.eclipse.keyple.plugin.remotese.transport.model;

/**
 * Default Transport Dto
 */
public class DefaultTransportDto implements TransportDto {

    final KeypleDto keypleDto;

    public DefaultTransportDto(KeypleDto keypleDto){
        this.keypleDto = keypleDto;
    }

    @Override
    public KeypleDto getKeypleDTO() {
        return keypleDto;
    }

    @Override
    public TransportDto nextTransportDTO(KeypleDto keypleDto) {
        return new DefaultTransportDto(keypleDto);
    }
}
