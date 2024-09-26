package node.connection.service;

import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection.entity.ConfigData;
import node.connection.hyperledger.FabricConfig;
import node.connection.repository.ConfigDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ConfigDataService {

    public static final String COURT_CHAIN_CODE = "court-chain-code";

    public static final String REGISTRY_CHAIN_CODE = "registry-chain-code";


    @Autowired
    private ConfigDataRepository configDataRepository;

    @Autowired
    private FabricConfig fabricConfig;


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

    public void updateCourtChainCodeVersion(String version) {
        this.updateValueByKey(COURT_CHAIN_CODE, version);
        this.fabricConfig.setCourtChainCodeVersion(version);
    }

    public void updateRegistryChainCodeVersion(String version) {
        this.updateValueByKey(REGISTRY_CHAIN_CODE, version);
        this.fabricConfig.setRegistryChainCodeVersion(version);
    }
}
