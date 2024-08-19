package node.connection.dto.wallet;

public record UserWalletCreateRequest(
        String phoneNumber,
        String password
) {
}
