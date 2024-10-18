package node.connection.dto.registry;

public record LandRightDescriptionDto(
        String address,
        String displayNumber,
        String landRightType,
        String landRightRatio,
        String registrationCause
) {}
