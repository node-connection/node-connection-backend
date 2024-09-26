package node.connection._core.security;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;

@Component
@Slf4j
public class JweDecoder {

    private static final String INFO_PREFIX = "NextAuth.js Generated Encryption Key";

    @Value("${security.jwe.secret}")
    private String secret;

    private final String salt = "";

    public String decode(String token) {
        KeyParameter parameter = getDerivedEncryptionKey(secret, salt);
        return decodeJwe(token, parameter);
    }

    // HKDF를 사용하여 키를 유도하는 메서드
    public byte[] hkdf(byte[] ikm, byte[] salt, byte[] info, int length) {
        // HKDF 초기화
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        HKDFParameters params = new HKDFParameters(ikm, salt, info);
        hkdf.init(params);

        byte[] result = new byte[length];
        hkdf.generateBytes(result, 0, result.length);
        return result;
    }

    // JavaScript의 getDerivedEncryptionKey에 해당하는 메서드
    public KeyParameter getDerivedEncryptionKey(String keyMaterial, String salt) {
        byte[] ikm = keyMaterial.getBytes(StandardCharsets.UTF_8);
        byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        byte[] info = (INFO_PREFIX + (salt.isEmpty() ? "" : " (" + salt + ")")).getBytes(StandardCharsets.UTF_8);
        byte[] derivedKey = hkdf(ikm, saltBytes, info, 32); // 32 bytes for A256GCM
        return new KeyParameter(derivedKey);
    }

    // JWE 디코딩 메서드
    public String decodeJwe(String jweString, KeyParameter encryptionKey) {
        JWEObject jweObject = null;
        try {
            jweObject = JWEObject.parse(jweString);

            DirectDecrypter decrypter = new DirectDecrypter(encryptionKey.getKey());
            jweObject.decrypt(decrypter);

            Payload payload = jweObject.getPayload();
            log.info("Jwe decoded: " + payload.toString());

            return payload.toString();
        } catch (ParseException | JOSEException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.JWT_DECODE_ERROR);
        }
    }
}