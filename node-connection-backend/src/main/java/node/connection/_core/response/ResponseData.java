package node.connection._core.response;

public record ResponseData<T>(
        Boolean success,
        T contents,
        ErrorData error
) {
}
