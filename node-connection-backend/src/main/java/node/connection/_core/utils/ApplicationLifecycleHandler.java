package node.connection._core.utils;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import node.connection.hyperledger.FabricConfig;
import node.connection.service.ConfigDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationLifecycleHandler implements ApplicationRunner {

    private final FabricConfig fabricConfig;

    private final ConfigDataService configDataService;


    public ApplicationLifecycleHandler(
            @Autowired FabricConfig fabricConfig,
            @Autowired ConfigDataService configDataService
    ) {
        this.fabricConfig = fabricConfig;
        this.configDataService = configDataService;
    }

    @Override
    public void run(ApplicationArguments args) {
        String issuanceVersion = this.configDataService.getIssuanceChainCodeVersion();
        String registryVersion = this.configDataService.getRegistryChainCodeVersion();

        this.fabricConfig.setIssuanceChainCodeVersion(issuanceVersion);
        this.fabricConfig.setRegistryChainCodeVersion(registryVersion);

        log.info("chain-code 버전 초기화 완료 | registry: {} | issuance: {}", registryVersion, issuanceVersion);
    }

    @PreDestroy
    public void close() {
    }
}
