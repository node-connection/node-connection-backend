package node.connection.dto.registry;

public record BuildingPartDescriptionDto(
        String address,
        String displayNumber,
        String receiptDate,
        String partNumber,
        String buildingDetails,
        String registrationCause
) {}
