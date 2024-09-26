package node.connection.dto.user.request;

public record UserRegisterRequest(
        String phoneNumber,
        String password
) {
}
