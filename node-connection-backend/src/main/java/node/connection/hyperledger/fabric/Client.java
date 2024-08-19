package node.connection.hyperledger.fabric;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.*;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection.hyperledger.fabric.ca.CAEnrollment;
import node.connection.hyperledger.fabric.util.FileUtils;
import org.hyperledger.fabric.sdk.User;

import java.io.IOException;
import java.util.Set;

@Builder
@Getter
@Setter
@ToString
public class Client implements User {

    @NonNull
    private String name;
    @NonNull
    private String mspId;
    @NonNull
    private CAEnrollment enrollment;

    @Override
    public String getName() {
        return name;
    }

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
    public CAEnrollment getEnrollment() {
        return enrollment;
    }

    @Override
    public String getMspId() {
        return mspId;
    }

    public void writeToFile(String path) throws IOException {
        FileUtils.write(path, toJson());
    }

    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(Client.class, new Serializer());
            mapper.registerModule(module);
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        }
    }

    public static Client fromFile(String path) {
        String json = FileUtils.read(path);
        return fromJson(json);
    }

    public static Client fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(Client.class, new Deserializer());
            mapper.registerModule(module);
            return mapper.readValue(json, Client.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        }
    }

    public static class Serializer extends JsonSerializer<Client> {
        @Override
        public void serialize(Client value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("name", value.getName());
            gen.writeStringField("mspId", value.getMspId());
            gen.writeStringField("enrollment", value.getEnrollment().serialize());
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<Client> {
        @Override
        public Client deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            JsonNode node = parser.readValueAsTree();
            String name = node.get("name").asText();
            String mspId = node.get("mspId").asText();
            String en = node.get("enrollment").asText();
            try {
                CAEnrollment enrollment = CAEnrollment.deserialize(en);
                return Client.builder()
                        .name(name)
                        .mspId(mspId)
                        .enrollment(enrollment)
                        .build();
            } catch (ClassNotFoundException e) {
                throw new IOException(e.getMessage());
            }
        }
    }
}
