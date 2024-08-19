package node.connection._core.response;

public class Response {
    public static <T> ResponseData<T> success(T contents) {
        return new ResponseData<>(true, contents, null);
    }

    public static <T> ResponseData<T> error(String status, String message) {
        ErrorData errorData = new ErrorData(status, message);
        return new ResponseData<>(false, null, errorData);
    }

    public static <T> ResponseData<T> of() {
        return success(null);
    }
}
