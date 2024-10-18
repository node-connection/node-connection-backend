package node.connection.dto.registry;

public record FirstSectionDto(
        String address,
        String rankNumber,
        String registrationPurpose,
        String receiptDate,
        String registrationCause,
        String holderAndAdditionalInfo
) {}

