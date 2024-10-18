package node.connection.dto.user.request;

public record IssuanceRequest(
        String address,
        String detailAddress
) {
}
