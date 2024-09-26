package node.connection.dto.registry.request;

public record FindUserRegistryDocumentsRequest(
        String phoneNumber,
        String password
) {
}
