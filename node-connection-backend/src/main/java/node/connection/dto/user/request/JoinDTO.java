package node.connection.dto.user.request;

import jakarta.annotation.Nullable;

public record JoinDTO (

    String username,

    String phoneNumber,

    String email,

    @Nullable
    String courtCode
) {}
