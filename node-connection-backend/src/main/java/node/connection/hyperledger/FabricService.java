package node.connection.hyperledger;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import node.connection.hyperledger.fabric.Client;
import node.connection.hyperledger.fabric.FabricConnector;
import node.connection.hyperledger.fabric.FabricPeer;
import node.connection.hyperledger.fabric.FabricProposalResponse;
import node.connection.hyperledger.fabric.ca.CAInfo;
import node.connection.hyperledger.fabric.ca.CAUser;
import node.connection.hyperledger.fabric.ca.FabricCAConnector;
import node.connection.hyperledger.fabric.ca.Registrar;
import node.connection.hyperledger.fabric.util.FileUtils;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric_ca.sdk.HFCAInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class FabricService {

    private final FabricConfig fabricConfig;

    private final CAInfo caInfo;

    private final FabricCAConnector fabricCAConnector;

    private final Registrar registrar;

    private FabricConnector fabricConnector;

    private node.connection.hyperledger.fabric.NetworkConfig networkConfig;

    private Client client;

    private final Channel channel;

    private final ObjectMapper objectMapper;


    public FabricService(
            @Autowired FabricConfig fabricConfig,
            @Autowired ObjectMapper objectMapper
    ) {
        this.fabricConfig = fabricConfig;
        this.objectMapper = objectMapper;
        this.caInfo = CAInfo.builder()
                .name(this.fabricConfig.getCaName())
                .url(this.fabricConfig.getCaUrl())
                .pemFile(this.fabricConfig.getCaPemFilePath())
                .allowAllHostNames(true)
                .build();

        this.fabricCAConnector = new FabricCAConnector(this.caInfo);
        HFCAInfo info = this.fabricCAConnector.info();
        log.debug("caName:{}, version:{}", info.getCAName(), info.getVersion());

        CAUser admin = CAUser.builder()
                .name(this.fabricConfig.getCaAdminName())
                .secret(this.fabricConfig.getCaAdminSecret())
                .build();

        this.registrar = this.fabricCAConnector.registrarEnroll(admin);
        String adminJson = registrar.toJson();
        FileUtils.write(this.fabricConfig.getMspFolder() + "/ca-admin.json", adminJson);
        log.info("fabric-ca admin 계정 enroll 완료");

        this.register();
        log.info("fabric-ca user 계정 enroll 완료");

        this.initialize();
        log.info("Fabric client 및 네트워크 설정 완료");
        log.debug("client: {}", this.client);

        this.fabricConnector = new FabricConnector(this.client);
        log.info("Fabric Connector 생성 완료");

        this.channel = fabricConnector.connectToChannel(this.networkConfig);
        log.info("채널 연결 완료");
    }

    public void register() {
        log.info("ca info: {}", this.fabricCAConnector.info());

        String mspId = this.fabricConfig.getUserMspId();
        String affiliation = this.fabricConfig.getUserAffiliation();
        Client client = fabricCAConnector.register(mspId, affiliation, this.registrar);
        String clientJson = client.toJson();
        FileUtils.write(this.fabricConfig.getUser1MspPath(), clientJson);
    }

    private void initialize() {
        String userJson = FileUtils.read(this.fabricConfig.getUser1MspPath());

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

        node.connection.hyperledger.fabric.NetworkConfig networkConfig = new node.connection.hyperledger.fabric.NetworkConfig.Builder()
                .channelName(this.fabricConfig.getChannelName())
                .peer(peer0Registry)
                .peer(peer0Viewer)
                .orderer(orderer)
                .build();

        this.client = Client.fromJson(userJson);
        this.networkConfig = networkConfig;
    }

    public void setChaincode(String name, String version) {
        this.fabricConnector.setChaincode(name, version);
    }

    public FabricProposalResponse invoke(String fcn, List<String> params) {
        return this.fabricConnector.invoke(fcn, params);
    }
}
