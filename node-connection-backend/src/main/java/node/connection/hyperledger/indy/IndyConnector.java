package node.connection.hyperledger.indy;

import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IndyConnector {

    private final Pool pool;
    private final WalletConfigFactory walletConfigFactory;
    private final IndyUtils indyUtils;

    public IndyConnector(
            @Autowired WalletConfigFactory walletConfigFactory,
            @Autowired IndyUtils indyUtils
    ) {
        this.walletConfigFactory = walletConfigFactory;
        this.indyUtils = indyUtils;
        this.pool = this.indyUtils.initializeIndyConnectionPool();
    }

    public void createUserWallet(String phoneNumber, String password) {
        String userConfig = this.walletConfigFactory.getUserWalletConfig(phoneNumber);
        this.indyUtils.createWallet(userConfig, password);
        log.info("일반 유저 지갑 생성 완료 | " + phoneNumber);
    }

    public void createCourtWallet(String court, String department, String location, String password) {
        String courtConfig = this.walletConfigFactory.getCourtWalletConfig(court, department, location);
        this.indyUtils.createWallet(courtConfig, password);
        log.info("등기소 지갑 생성 완료 | " + court + " | " + department + " | " + location);
    }

    public Wallet openUserWallet(String phoneNumber, String password) {
        String config = this.walletConfigFactory.getUserWalletConfig(phoneNumber);
        return this.indyUtils.openWallet(config, password);
    }

    public Wallet openCourtWallet(String court, String department, String location, String password) {
        String config = this.walletConfigFactory.getCourtWalletConfig(court, department, location);
        return this.indyUtils.openWallet(config, password);
    }
}
