package node.connection._core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Mapper {
    private final ObjectMapper objectMapper;

    public Mapper(@Autowired ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String writeValueAsString(Object obj) {
        try {
            return this.objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        }
    }

    public <T> T readValue(String src, Class<T> valueType) {
        try {
            return this.objectMapper.readValue(src, valueType);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        }
    }

    public <T> T readValue(String src, TypeReference<T> valueTypeRef) {
        try {
            return this.objectMapper.readValue(src, valueTypeRef);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        }
    }

    public JsonNode writeValueAsJsonNode(Object obj) {
        try {
            String json = this.objectMapper.writeValueAsString(obj);
            return this.objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        }
    }

    public ArrayNode createArrayNode() {
        return this.objectMapper.createArrayNode();
    }
}
