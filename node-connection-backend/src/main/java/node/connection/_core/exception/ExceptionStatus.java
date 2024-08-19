package node.connection._core.exception;

import com.google.api.Http;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ExceptionStatus {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "1000", "서버에서 알 수 없는 에러가 발생했습니다."),
    INVALID_METHOD_ARGUMENTS_ERROR(HttpStatus.BAD_REQUEST, "1001", ""),
    OBJECT_SERIALIZE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "1002", ""),
    OBJECT_DESERIALIZE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "1003", ""),
    JSON_PROCESSING_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "1004", "JSON 파싱 에러 발생"),
    FILE_IO_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "1005", "파일 입출력 과정에서 에러가 발생했습니다."),

    INDY_INITIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "2000", "Indy 초기화 중 에러가 발생했습니다."),
    WALLET_CREATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "2001", "지갑 생성 중 에러가 발생했습니다."),
    WALLET_OPEN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "2002", "지갑 조회 중 에러가 발생했습니다."),

    FABRIC_CA_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3000", "CA 초기화 중 에러가 발생했습니다."),
    FABRIC_CA_REGISTER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3001", "CA 가입 중 에러가 발생했습니다."),
    FABRIC_CA_ENROLL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3002", "CA 등록 중 에러가 발생했습니다."),

    FABRIC_CONNECTION_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3001", "Ledger 연결 초기화 중 에러가 발생했습니다."),
    FABRIC_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3002", "Ledger 연결 중 에러가 발생했습니다."),
    FABRIC_CLIENT_CREATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3003", "Fabric client 생성 중 에러가 발생했습니다."),
    INVALID_PROTOCOL_BUFFER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3004", "프로토콜 버퍼가 유효하지 않습니다."),
    FABRIC_CHANNEL_CREATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3005", "Fabric channel 생성 중 에러가 발생했습니다."),
    FABRIC_TRANSACTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3006", "트랜잭션 실행 과정에서 에러가 발생했습니다."),
    FABRIC_CHAINCODE_INSTALLATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3007", "체인코드 설치 과정에서 에러가 발생했습니다."),
    FABRIC_CHAINCODE_INSTANTIATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3008", "체인코드 인스턴스화 과정에서 에러가 발생했습니다."),
    PROPOSAL_RESPONSE_INTERCEPTOR_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3009", "Proposal response 처리 과정에서 에러가 발생했습니다."),
    FABRIC_CHAINCODE_UPGRADE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3010", "체인코드 업그레이드 과정에서 에러가 발생했습니다."),
    FABRIC_QUERY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3011", "쿼리 중 에러가 발생했습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
