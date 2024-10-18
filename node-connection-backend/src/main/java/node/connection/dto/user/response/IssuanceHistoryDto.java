package node.connection.dto.user.response;

import java.time.LocalDateTime;

public record IssuanceHistoryDto(
        String hash,
        String address,
        String detailAddress,
        LocalDateTime issuanceAt,
        LocalDateTime expiredAt
) {
}
