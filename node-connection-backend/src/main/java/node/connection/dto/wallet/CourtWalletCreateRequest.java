package node.connection.dto.wallet;

public record CourtWalletCreateRequest(
        String court,
        String department,
        String location,
        String password
) {
}
