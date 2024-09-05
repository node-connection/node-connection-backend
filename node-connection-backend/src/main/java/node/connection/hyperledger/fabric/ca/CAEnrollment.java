package node.connection.hyperledger.fabric.ca;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import node.connection._core.utils.Mapper;
import org.bouncycastle.util.encoders.Base64;
import org.hyperledger.fabric.sdk.Enrollment;

import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Objects;

@Builder
@Getter
@Setter
@ToString
public class CAEnrollment implements Enrollment, Serializable {

    private PrivateKey key;
    private String cert;

    @Override
    public PrivateKey getKey() {
        return key;
    }

    @Override
    public String getCert() {
        return cert;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CAEnrollment that)) return false;
        return Objects.equals(key, that.key) &&
                Objects.equals(cert, that.cert);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, cert);
    }

    public static CAEnrollment of(Enrollment e) {
        return CAEnrollment.builder()
                .key(e.getKey())
                .cert(e.getCert())
                .build();
    }

    public String serialize(Mapper mapper) {
        return mapper.writeValueAsString(this);
    }

    public static CAEnrollment deserialize(Mapper mapper, String json) {
        return mapper.readValue(json, CAEnrollment.class);
    }

    public static class Serializer extends JsonSerializer<CAEnrollment> {
        @Override
        public void serialize(CAEnrollment value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("key", serializePrivateKey(value.getKey()));
            gen.writeStringField("cert", value.getCert());
            gen.writeEndObject();
        }

        private String serializePrivateKey(PrivateKey key) {
            byte[] encodedKey = key.getEncoded();
            return java.util.Base64.getEncoder().encodeToString(encodedKey);
        }
    }

    public static class DeSerializer extends JsonDeserializer<CAEnrollment> {

        @Override
        public CAEnrollment deserialize(JsonParser p, DeserializationContext context) throws IOException {
            JsonNode node = p.readValueAsTree();
            String key = node.get("key").asText();
            String cert = node.get("cert").asText();
            try {
                return CAEnrollment.builder()
                        .key(deserializePrivateKey(key))
                        .cert(cert)
                        .build();
            } catch (GeneralSecurityException e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        private PrivateKey deserializePrivateKey(String key) throws GeneralSecurityException {
            byte[] decodedKey = Base64.decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("EC"); // 혹은 "RSA" 또는 사용 중인 알고리즘
            return keyFactory.generatePrivate(keySpec);
        }
    }
}
