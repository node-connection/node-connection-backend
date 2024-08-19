package node.connection._core.exception;

import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.client.BadRequestException;
import node.connection._core.exception.client.ForbiddenException;
import node.connection._core.exception.client.NotFoundException;
import node.connection._core.exception.client.UnauthorizedException;
import node.connection._core.exception.server.ServerException;
import node.connection._core.response.ErrorData;
import node.connection._core.response.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static ResponseEntity<? extends ResponseData<?>> createExceptionResponseData(CustomException exception) {
        HttpStatus status = exception.status();
        ResponseData<?> data = exception.body();
        return new ResponseEntity<>(data, status);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> badRequest(BadRequestException exception) {
        return createExceptionResponseData(exception);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> unAuthorized(UnauthorizedException exception) {
        return createExceptionResponseData(exception);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> forbidden(ForbiddenException exception) {
        return createExceptionResponseData(exception);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> notFound(NotFoundException exception) {
        return createExceptionResponseData(exception);
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<?> serverError(ServerException exception) {
        return createExceptionResponseData(exception);
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class,
                        MethodArgumentTypeMismatchException.class })
    public ResponseEntity<?> methodArgumentNotValid(Exception exception) {
        String message = exception.getMessage();
        String code = ExceptionStatus.INVALID_METHOD_ARGUMENTS_ERROR.getCode();

        ErrorData errorData = new ErrorData(code, message);
        ResponseData<?> responseData = new ResponseData<>(false, null, errorData);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(responseData, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> exception(Exception exception) {
        log.error("[exception handler]", exception);
        CustomException customException = new ServerException(ExceptionStatus.INTERNAL_SERVER_ERROR);
        return createExceptionResponseData(customException);
    }
}
