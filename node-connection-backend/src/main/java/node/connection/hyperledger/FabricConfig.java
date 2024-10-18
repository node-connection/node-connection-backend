package node.connection.hyperledger;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class FabricConfig {

    public static String ISSUANCE_CHAIN_CODE = "issuance";

    public static String REGISTRY_CHAIN_CODE = "registry";

    private String registryChainCodeVersion = "1.0.0";

    private String issuanceChainCodeVersion = "1.0.0";

    @Value("${hyperledger.fabric.pem}")
    private String pemFilePath;

    @Value("${hyperledger.fabric.ca.registry.name}")
    private String registryCaName;

    @Value("${hyperledger.fabric.ca.registry.url}")
    private String registryCaUrl;

    @Value("${hyperledger.fabric.ca.registry.pem}")
    private String registryCaPemFilePath;

    @Value("${hyperledger.fabric.ca.viewer.name}")
    private String viewerCaName;

    @Value("${hyperledger.fabric.ca.viewer.url}")
    private String viewerCaUrl;

    @Value("${hyperledger.fabric.ca.viewer.pem}")
    private String viewerCaPemFilePath;

    @Value("${hyperledger.fabric.ca.admin.name}")
    private String caAdminName;

    @Value("${hyperledger.fabric.ca.admin.password}")
    private String caAdminSecret;

    @Value("${hyperledger.fabric.root.msp}")
    private String rootMsp;

    @Value("${hyperledger.fabric.root.number}")
    private String rootNumber;

    @Value("${hyperledger.fabric.root.password}")
    private String rootPassword;

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

    public void setRegistryChainCodeVersion(String version) {
        this.registryChainCodeVersion = version;
    }

    public void setIssuanceChainCodeVersion(String version) {
        this.issuanceChainCodeVersion = version;
    }
}
