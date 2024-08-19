package node.connection._core.exception.client;

import node.connection._core.exception.CustomException;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.response.ErrorData;
import node.connection._core.response.ResponseData;
import org.springframework.http.HttpStatus;

public class BadRequestException extends RuntimeException implements CustomException {

    private final ExceptionStatus exception;


    public BadRequestException(ExceptionStatus exception) {
        super(exception.getMessage());
        this.exception = exception;
    }

    @Override
    public ResponseData<?> body() {
        ErrorData errorData = new ErrorData(exception.getCode(), exception.getMessage());
        return new ResponseData<>(false, null, errorData);
    }

    @Override
    public HttpStatus status() {
        return exception.getStatus();
    }

    @Override
    public String code() {
        return exception.getCode();
    }
}
