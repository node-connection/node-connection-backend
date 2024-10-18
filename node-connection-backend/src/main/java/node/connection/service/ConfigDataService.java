package node.connection.service;

import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection._core.security.CustomUserDetails;
import node.connection._core.utils.AccessControl;
import node.connection.entity.ConfigData;
import node.connection.hyperledger.FabricConfig;
import node.connection.repository.ConfigDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ConfigDataService {

    public static final String ISSUANCE_CHAIN_CODE = "issuance-chain-code";

    public static final String REGISTRY_CHAIN_CODE = "registry-chain-code";


    @Autowired
    private ConfigDataRepository configDataRepository;

    @Autowired
    private FabricConfig fabricConfig;

    @Autowired
    private AccessControl accessControl;


    // key로 value 조회
    public Optional<String> getValueByKey(String key) {
        return configDataRepository.findByKey(key).map(ConfigData::getValue);
    }

    // key로 value 업데이트
    private void updateValueByKey(String key, String newValue) {
        Optional<ConfigData> configDataOpt = configDataRepository.findByKey(key);
        if (configDataOpt.isPresent()) {
            ConfigData configData = configDataOpt.get();
            configData.setValue(newValue);
            configDataRepository.save(configData);
        } else {
            log.error("key not found: {}", key);
            throw new ServerException(ExceptionStatus.KEY_NOT_FOUND);
        }
    }

    public String getIssuanceChainCodeVersion() {
        return this.getValueByKey(ISSUANCE_CHAIN_CODE)
                .orElseThrow(() -> new ServerException(ExceptionStatus.KEY_NOT_FOUND));
    }

    public String getRegistryChainCodeVersion() {
        return this.getValueByKey(REGISTRY_CHAIN_CODE)
                .orElseThrow(() -> new ServerException(ExceptionStatus.KEY_NOT_FOUND));
    }

    public void updateIssuanceChainCodeVersion(CustomUserDetails userDetails, String version) {
        this.accessControl.hasRootRole(userDetails);
        this.updateValueByKey(ISSUANCE_CHAIN_CODE, version);
        this.fabricConfig.setIssuanceChainCodeVersion(version);
    }

    public void updateRegistryChainCodeVersion(CustomUserDetails userDetails, String version) {
        this.accessControl.hasRootRole(userDetails);
        this.updateValueByKey(REGISTRY_CHAIN_CODE, version);
        this.fabricConfig.setRegistryChainCodeVersion(version);
    }
}
