package node.connection._core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ExceptionStatus {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "1000", "인증되지 않았습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "1001", "권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "1002", "서버에서 알 수 없는 에러가 발생했습니다."),
    INVALID_METHOD_ARGUMENTS_ERROR(HttpStatus.BAD_REQUEST, "1003", ""),
    OBJECT_SERIALIZE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "1004", ""),
    OBJECT_DESERIALIZE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "1005", ""),
    JSON_PROCESSING_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "1006", "JSON 파싱 에러 발생"),
    FILE_IO_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "1007", "파일 입출력 과정에서 에러가 발생했습니다."),
    JWT_DECODE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "1008", "JWT 디코드 과정에서 에러가 발생했습니다."),
    KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "1010", "키를 찾을 수 없습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "2000", "존재하지 않는 유저입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "2001", "비밀번호가 일치하지 않습니다."),
    INVALID_MSP_ID(HttpStatus.BAD_REQUEST, "2002", "잘못된 MSP ID 입니다."),
    ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "2003", "이미 가입된 유저입니다."),
    INVALID_COURT_CODE(HttpStatus.BAD_REQUEST, "2004", "존재하지 않는 코드입니다."),

    FABRIC_CA_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3000", "CA 초기화 중 에러가 발생했습니다."),
    FABRIC_CA_REGISTER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3001", "CA 가입 중 에러가 발생했습니다."),
    FABRIC_CA_ENROLL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3002", "CA 등록 중 에러가 발생했습니다."),
    NO_FABRIC_CA_DATA(HttpStatus.INTERNAL_SERVER_ERROR, "3003", "CA 가입 데이터가 없습니다."),
    ALREADY_CA_REGISTERED(HttpStatus.BAD_REQUEST, "3004", "이미 CA에 가입되었습니다."),
    FABRIC_CA_REVOKE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3005", "CA 삭제 중 에러가 발생했습니다."),
    FABRIC_CA_UPDATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3006", "CA 업데이트 중 에러가 발생했습니다."),

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
    FABRIC_QUERY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3011", "패브릭 쿼리 중 에러가 발생했습니다."),
    FABRIC_INVOKE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3012", "패브릭 함수 호출 중 에러가 발생했습니다."),

    NOT_SUPPORT_LOCATION(HttpStatus.BAD_REQUEST, "4000", "지원하지 않는 지역입니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
