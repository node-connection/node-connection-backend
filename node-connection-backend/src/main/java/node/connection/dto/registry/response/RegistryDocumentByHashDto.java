package node.connection.dto.registry.response;

import node.connection.dto.registry.RegistryDocumentDto;

import java.time.LocalDateTime;

public record RegistryDocumentByHashDto(
        String txId,
        String issuer,
        LocalDateTime issuanceAt,
        LocalDateTime expiredAt,
        RegistryDocumentDto hashedDocument,
        RegistryDocumentDto latestDocument
) {
}
