package node.connection.hyperledger.fabric.ca;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection.hyperledger.fabric.util.FileUtils;
import org.hyperledger.fabric.sdk.User;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

@Builder
@Getter
@Setter
public class Registrar implements User {
    private String name;
    private CAEnrollment enrollment;

    @Override
    public Set<String> getRoles() {
        return null;
    }

    @Override
    public String getAccount() {
        return null;
    }

    @Override
    public String getAffiliation() {
        return null;
    }

    @Override
    public String getMspId() {
        return null;
    }

    public void writeToFile(String path) {
        FileUtils.write(path, toJson());
    }

    public static Registrar fromFile(String path) {
        String json = FileUtils.read(path);
        return fromJson(json);
    }

    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(Registrar.class, new Serializer());
            mapper.registerModule(module);
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        }
    }

    public static Registrar fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(Registrar.class, new Deserializer());
            mapper.registerModule(module);
            return mapper.readValue(json, Registrar.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        }
    }

    public static class Serializer extends JsonSerializer<Registrar> {
        @Override
        public void serialize(Registrar value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("name", value.getName());
            gen.writeStringField("enrollment", value.getEnrollment().serialize());
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<Registrar> {
        @Override
        public Registrar deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            JsonNode node = parser.readValueAsTree();
            String name = node.get("name").asText();
            String en = node.get("enrollment").asText();
            try {
                CAEnrollment enrollment = CAEnrollment.deserialize(en);
                return Registrar.builder()
                        .name(name)
                        .enrollment(enrollment)
                        .build();
            } catch (ClassNotFoundException e) {
                throw new IOException(e.getMessage());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Registrar registrar)) return false;
        return Objects.equals(name, registrar.name) &&
                Objects.equals(enrollment, registrar.enrollment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, enrollment);
    }
}
