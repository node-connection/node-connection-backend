package node.connection.dto.wallet;

public record CourtWalletCreateRequest(
        String court,
        String support,
        String office,
        String password
) {
}
