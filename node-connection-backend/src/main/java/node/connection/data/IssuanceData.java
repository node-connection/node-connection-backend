package node.connection.data;

import node.connection.dto.registry.RegistryDocumentDto;

import java.time.LocalDateTime;

public record IssuanceData(
        String txId,
        String issuerName,
        String issuerDataHash,
        RegistryDocumentDto registryDocument,
        LocalDateTime issuanceDate,
        LocalDateTime expirationDate
) {
}
