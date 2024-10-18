package node.connection.hyperledger.fabric.ca;

import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection.entity.UserAccount;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.Attribute;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.HFCAInfo;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.*;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Properties;

@Slf4j
public class FabricCAConnector {

    private final HFCAClient caClient;

    public FabricCAConnector(
            CAInfo caInfo
    ) {
        Properties properties = new Properties();
        if (caInfo.isAllowAllHostNames()) {
            properties.put("allowAllHostNames", "true");
        }

        if (caInfo.getPemFile() != null) {
            properties.put("pemFile", caInfo.getPemFile());
        }

        try {
            caClient = HFCAClient.createNewInstance(caInfo.getName(), caInfo.getUrl(), properties);
        } catch (MalformedURLException | InvalidArgumentException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CA_CONFIGURATION_ERROR);
        }

        try {
            caClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException |
                CryptoException | org.hyperledger.fabric.sdk.exception.InvalidArgumentException |
                NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CA_CONFIGURATION_ERROR);
        }
    }

    public HFCAClient getClient() {
        return caClient;
    }

    public HFCAInfo info() {
        try {
            return caClient.info();
        } catch (InfoException | InvalidArgumentException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CA_CONFIGURATION_ERROR);
        }
    }

    public Registrar registrarEnroll(CAUser caUser) {
        try {
            Enrollment e = caClient.enroll(caUser.getName(), caUser.getSecret());
            return Registrar.builder()
                    .name(caUser.getName())
                    .enrollment(CAEnrollment.of(e))
                    .build();
        } catch (EnrollmentException | InvalidArgumentException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CA_REGISTER_ERROR);
        }
    }

    public String register(String id, String secret, String role, Registrar registrar) {
        try {
            RegistrationRequest request = new RegistrationRequest(id);
            request.setSecret(secret);
            request.addAttribute(new Attribute("role", role));

            String response = null;
            try {
                response = caClient.register(request, registrar);
            } catch(RegistrationException ignored) {}

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CA_REGISTER_ERROR);
        }
    }

    public Enrollment enroll(CAUser enrollment) {
        return this.enroll(enrollment.getName(), enrollment.getSecret());
    }

    public Enrollment enroll(UserAccount register) {
        return this.enroll(register.getFabricId(), register.getSecret());
    }

    public Enrollment enroll(String name, String secret) {
        try {
            return caClient.enroll(name, secret);
        } catch (EnrollmentException | InvalidArgumentException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CA_ENROLL_ERROR);
        }
    }

    public Enrollment reenroll(Registrar registrar) {
        try {
            return this.caClient.reenroll(registrar);
        } catch (EnrollmentException | InvalidArgumentException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CA_ENROLL_ERROR);
        }
    }

    public void revoke(User registrar, Enrollment enrollment, String reason) {
        try {
            this.caClient.revoke(registrar, enrollment, reason);
        } catch (RevocationException | InvalidArgumentException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CA_REVOKE_ERROR);
        }
    }

    public void revoke(User registrar, String revokee, String reason) {
        try {
            this.caClient.revoke(registrar, revokee, reason, true);
        } catch (RevocationException | InvalidArgumentException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CA_REVOKE_ERROR);
        }
    }
}
