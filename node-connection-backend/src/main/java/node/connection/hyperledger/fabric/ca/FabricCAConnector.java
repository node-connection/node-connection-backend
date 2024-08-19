package node.connection.hyperledger.fabric.ca;

import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection.hyperledger.fabric.Client;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.Attribute;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.HFCAInfo;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InfoException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Properties;

@Slf4j
public class FabricCAConnector {

    private final HFCAClient caClient;

    public FabricCAConnector(CAInfo caInfo) {
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

    public Registrar registrarEnroll(CAUser enrollment) {
        try {
            Enrollment e = caClient.enroll(enrollment.getName(), enrollment.getSecret());
            return Registrar.builder()
                    .name(enrollment.getName())
                    .enrollment(CAEnrollment.of(e))
                    .build();
        } catch (EnrollmentException | InvalidArgumentException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CA_REGISTER_ERROR);
        }
    }

    public Client register(String mspId, String affiliation, Registrar registrar) {
        try {
            String name = mspId + "-api-" + System.currentTimeMillis();
            RegistrationRequest request = new RegistrationRequest(
                    name, affiliation);
            request.addAttribute(new Attribute("role", HFCAClient.HFCA_TYPE_CLIENT));
            String secret = caClient.register(request, registrar);
            Enrollment e = enroll(CAUser.builder()
                    .name(request.getEnrollmentID())
                    .secret(secret)
                    .build());
            return Client.builder()
                    .name(name)
                    .mspId(mspId)
                    .enrollment(CAEnrollment.of(e))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CA_REGISTER_ERROR);
        }
    }

    public Enrollment enroll(CAUser enrollment) {
        try {
            return caClient.enroll(enrollment.getName(), enrollment.getSecret());
        } catch (EnrollmentException | InvalidArgumentException e) {
            e.printStackTrace();
            throw new ServerException(ExceptionStatus.FABRIC_CA_ENROLL_ERROR);
        }
    }

}
