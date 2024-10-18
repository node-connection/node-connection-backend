package node.connection.dto.registry;

public record BuildingDescriptionDto(
        String address,
        String displayNumber,
        String receiptDate,
        String locationNumber,
        String buildingDetails,
        String registrationCause
) {}
