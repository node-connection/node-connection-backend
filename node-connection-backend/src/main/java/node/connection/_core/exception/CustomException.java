package node.connection._core.exception;

import node.connection._core.response.ResponseData;
import org.springframework.http.HttpStatus;

public interface CustomException {
    ResponseData<?> body();
    HttpStatus status();
    String code();
}
