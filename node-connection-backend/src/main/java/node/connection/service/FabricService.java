package node.connection.service;

import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.client.BadRequestException;
import node.connection._core.exception.client.NotFoundException;
import node.connection._core.exception.server.ServerException;
import node.connection._core.security.CustomUserDetails;
import node.connection._core.utils.Mapper;
import node.connection.entity.UserAccount;
import node.connection.entity.constant.Role;
import node.connection.hyperledger.FabricConfig;
import node.connection.hyperledger.fabric.*;
import node.connection.hyperledger.fabric.ca.*;
import node.connection.repository.UserAccountRepository;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FabricService {

    private final FabricConfig fabricConfig;

    private final FabricCAConnector registryCAConnector;

    private final FabricCAConnector viewerCAConnector;

    private final Registrar registrar;

    private final Registrar viewerRegistrar;

    private final FabricConnector fabricConnector;

    private NetworkConfig networkConfig;

    private Client rootClient;

    private final Mapper objectMapper;

    private final PasswordEncoder passwordEncoder;

    private final UserAccountRepository userAccountRepository;

    public final static String VIEWER_MSP = "ViewerMSP";

    public final static String REGISTRY_MSP = "RegistryMSP";

    public final static String ID_DELIMITER = ".api.";

    public FabricService(@Autowired FabricConfig fabricConfig,
                         @Autowired Mapper objectMapper,
                         @Autowired PasswordEncoder passwordEncoder,
                         @Autowired UserAccountRepository userAccountRepository
    ) {
        this.fabricConfig = fabricConfig;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.userAccountRepository = userAccountRepository;

        CAInfo registryCaInfo = CAInfo.builder()
                .name(this.fabricConfig.getRegistryCaName())
                .url(this.fabricConfig.getRegistryCaUrl())
                .pemFile(this.fabricConfig.getRegistryCaPemFilePath())
                .allowAllHostNames(true)
                .build();

        CAInfo viewerCaInfo = CAInfo.builder()
                .name(this.fabricConfig.getViewerCaName())
                .url(this.fabricConfig.getViewerCaUrl())
                .pemFile(this.fabricConfig.getViewerCaPemFilePath())
                .allowAllHostNames(true)
                .build();

        this.registryCAConnector = new FabricCAConnector(registryCaInfo);
        this.viewerCAConnector = new FabricCAConnector(viewerCaInfo);

        CAUser admin = CAUser.builder()
                .name(this.fabricConfig.getCaAdminName())
                .secret(this.fabricConfig.getCaAdminSecret())
                .build();

        this.registrar = this.registryCAConnector.registrarEnroll(admin);
        this.viewerRegistrar = this.viewerCAConnector.registrarEnroll(admin);
        String registryRegistrarEnrollment = this.registrar.getEnrollment().serialize(this.objectMapper);
        String encodedSecret = this.passwordEncoder.encode(admin.getSecret());
        this.userAccountRepository.save(UserAccount.of(this.registrar, encodedSecret, registryRegistrarEnrollment));
        log.info("fabric-ca admin 계정 enroll 완료");

        String registryId = getId(REGISTRY_MSP, this.fabricConfig.getRootNumber());
        this.registryCAConnector.register(registryId, this.fabricConfig.getRootPassword(), HFCAClient.HFCA_TYPE_CLIENT, this.registrar);

        Enrollment e = this.registryCAConnector.enroll(registryId, this.fabricConfig.getRootPassword());
        CAEnrollment caEnrollment = CAEnrollment.of(e);
        Client client = Client.builder()
                .name(registryId)
                .mspId(REGISTRY_MSP)
                .enrollment(caEnrollment)
                .build();

        String rootEnrollment = caEnrollment.serialize(objectMapper);
        encodedSecret = this.passwordEncoder.encode(this.fabricConfig.getRootPassword());
        this.userAccountRepository.save(UserAccount.of(client, this.fabricConfig.getRootNumber(), encodedSecret, rootEnrollment, Role.ROOT));
        log.info("fabric-ca root 계정 enroll 완료");

        this.initialize();
        log.info("Fabric client 및 네트워크 설정 완료");
        log.debug("client: {}", this.rootClient);

        this.fabricConnector = new FabricConnector(this.rootClient);
        log.info("Fabric Connector 생성 완료");

        fabricConnector.connectToChannel(this.networkConfig);
        log.info("채널 연결 완료");
    }

    public FabricConnector getRootFabricConnector() {
        return fabricConnector;
    }

    public NetworkConfig getNetworkConfig() {
        return this.networkConfig;
    }

    public Enrollment register(CustomUserDetails userDetails) {
        String name = userDetails.getUsername();
        String mspId = userDetails.getUserAccount().getMspId();
        String number = name.split(ID_DELIMITER)[1];
        String password = userDetails.getPassword();

        Enrollment enrollment;
        if (Objects.equals(mspId, VIEWER_MSP)) {
            enrollment = this.registerToViewerMSP(number, password);
        }
        else if (Objects.equals(mspId, REGISTRY_MSP)) {
            enrollment = this.registerToRegistryMSP(number, password);
        }
        else {
            throw new BadRequestException(ExceptionStatus.INVALID_MSP_ID);
        }

        return enrollment;
    }

    public Enrollment registerToViewerMSP(String phoneNumber, String secret) {
        String id = getId(VIEWER_MSP, phoneNumber);
        String response = this.viewerCAConnector.register(id, secret, HFCAClient.HFCA_TYPE_USER, this.viewerRegistrar);

        Enrollment enrollment;

        if (response == null) {
            log.info("id {} already registered. try re-enroll.", id);

            UserAccount userAccount = this.userAccountRepository.findByFabricId(id)
                    .orElseThrow(() -> new NotFoundException(ExceptionStatus.USER_NOT_FOUND));

            Registrar newRegistrar = Registrar.builder()
                    .name(id)
                    .enrollment(CAEnrollment.deserialize(this.objectMapper, userAccount.getEnrollment()))
                    .build();

            enrollment = this.viewerCAConnector.reenroll(newRegistrar);
        }
        else {
            enrollment = this.viewerCAConnector.enroll(id, secret);
        }

        return enrollment;
    }

    public Enrollment registerToRegistryMSP(String number, String secret) {
        String id = getId(REGISTRY_MSP, number);
        String response = this.registryCAConnector.register(id, secret, HFCAClient.HFCA_TYPE_CLIENT, this.registrar);
        if (response == null) {
            throw new BadRequestException(ExceptionStatus.ALREADY_CA_REGISTERED);
        }

        return this.registryCAConnector.enroll(id, secret);
    }

    public void enroll(String mspId, String id, String secret) {
        if (mspId.equals(REGISTRY_MSP)) {
            this.registryCAConnector.enroll(id, secret);
        }
        else if (mspId.equals(VIEWER_MSP)) {
            this.viewerCAConnector.enroll(id, secret);
        }
        else {
            throw new BadRequestException(ExceptionStatus.INVALID_MSP_ID);
        }
    }

    private void initialize() {
        String id = getId(this.fabricConfig.getRootMsp(), this.fabricConfig.getRootNumber());
        UserAccount register = this.userAccountRepository.findById(id)
                .orElseThrow(() -> new ServerException(ExceptionStatus.NO_FABRIC_CA_DATA));

        CAEnrollment enrollment = CAEnrollment.deserialize(this.objectMapper, register.getEnrollment());

        FabricPeer peer0Registry = FabricPeer.builder()
                .name(this.fabricConfig.getRegistryName())
                .url(this.fabricConfig.getRegistryUrl())
                .pemFile(this.fabricConfig.getRegistryPemFilePath())
                .hostnameOverride(this.fabricConfig.getRegistryName())
                .build();

        FabricPeer peer0Viewer = FabricPeer.builder()
                .name(this.fabricConfig.getViewerName())
                .url(this.fabricConfig.getViewerUrl())
                .pemFile(this.fabricConfig.getViewerPemFilePath())
                .hostnameOverride(this.fabricConfig.getViewerName())
                .build();

        FabricPeer orderer = FabricPeer.builder()
                .name(this.fabricConfig.getOrdererName())
                .url(this.fabricConfig.getOrdererUrl())
                .pemFile(this.fabricConfig.getOrdererPemFilePath())
                .hostnameOverride(this.fabricConfig.getOrdererName())
                .build();

        NetworkConfig networkConfig = new NetworkConfig.Builder()
                .channelName(this.fabricConfig.getChannelName())
                .peer(peer0Registry)
                .peer(peer0Viewer)
                .orderer(orderer)
                .build();

        this.rootClient = Client.of(register, enrollment);
        this.networkConfig = networkConfig;
    }

    public FabricConnector getConnectorByIdAndChannel(String id, String channel) {
        UserAccount register = this.userAccountRepository.findById(id)
                .orElseThrow(() -> new ServerException(ExceptionStatus.NO_FABRIC_CA_DATA));

        CAEnrollment enrollment = CAEnrollment.deserialize(this.objectMapper, register.getEnrollment());
        Client client = Client.of(register, enrollment);
        FabricConnector connector = new FabricConnector(client);
        this.networkConfig.setChannelName(channel);
        connector.connectToChannel(this.networkConfig);

        return connector;
    }

    public void setChaincode(String name, String version) {
        this.fabricConnector.setChaincode(name, version);
    }

    public FabricProposalResponse invoke(String fcn, List<String> params) {
        return this.fabricConnector.invoke(fcn, params);
    }

    public FabricProposalResponse query(String fcn, List<String> params) {
        return this.fabricConnector.query(fcn, params);
    }

    public static String getId(String msp, String number) {
        return msp + ID_DELIMITER + number;
    }
}
