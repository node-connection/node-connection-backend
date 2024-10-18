package node.connection.dto.registry;

public record LandDescriptionDto(
        String address,
        String displayNumber,
        String locationNumber,
        String landType,
        String area,
        String registrationCause
) {}
