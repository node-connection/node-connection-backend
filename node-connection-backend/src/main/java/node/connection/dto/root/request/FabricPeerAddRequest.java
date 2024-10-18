package node.connection.dto.root.request;

public record FabricPeerAddRequest(
        String name,
        String url,
        String pem
) {
}
