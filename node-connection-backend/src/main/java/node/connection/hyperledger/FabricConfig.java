package node.connection.hyperledger;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Getter
public class FabricConfig {
    private final String mspFolder = "./msp";

    @Value("${hyperledger.fabric.ca.name}")
    private String caName;

    @Value("${hyperledger.fabric.ca.url}")
    private String caUrl;

    @Value("${hyperledger.fabric.ca.pem}")
    private String caPemFilePath;

    @Value("${hyperledger.fabric.ca.admin.name}")
    private String caAdminName;

    @Value("${hyperledger.fabric.ca.admin.password}")
    private String caAdminSecret;

    @Value("${hyperledger.fabric.user.msp}")
    private String userMspId;

    @Value("${hyperledger.fabric.user.affiliation}")
    private String userAffiliation;

    private final String user1MspName = "user1.json";
    private final String user1MspPath = mspFolder + File.separator + user1MspName;

    @Value("${hyperledger.fabric.organization.registry.name}")
    private String registryName;

    @Value("${hyperledger.fabric.organization.registry.url}")
    private String registryUrl;

    @Value("${hyperledger.fabric.organization.registry.pem}")
    private String registryPemFilePath;

    @Value("${hyperledger.fabric.organization.viewer.name}")
    private String viewerName;

    @Value("${hyperledger.fabric.organization.viewer.url}")
    private String viewerUrl;

    @Value("${hyperledger.fabric.organization.viewer.pem}")
    private String viewerPemFilePath;

    @Value("${hyperledger.fabric.organization.orderer.name}")
    private String ordererName;

    @Value("${hyperledger.fabric.organization.orderer.url}")
    private String ordererUrl;

    @Value("${hyperledger.fabric.organization.orderer.pem}")
    private String ordererPemFilePath;

    @Value("${hyperledger.fabric.channel.name}")
    private String channelName;
}
