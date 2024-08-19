package node.connection.hyperledger.indy;

import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class IndyUtils {

    private static final String POOL_NAME = "NodeConnectionPool";
    private static final String POOL_CONFIG = "{\"genesis_txn\": \"src/main/java/node/connection/utils/indy/pool.txn\"}";

    private final WalletConfigFactory walletConfigFactory;

    public IndyUtils(@Autowired WalletConfigFactory walletConfigFactory) {
        this.walletConfigFactory = walletConfigFactory;
    }

    public Pool initializeIndyConnectionPool() {
        Pool pool = null;

        try {
            Pool.setProtocolVersion(2);
            pool = Pool.openPoolLedger(POOL_NAME, "{}").get();
        } catch (Exception exception) {
            try {
                log.info("Hyperledger Indy pool create using " + POOL_CONFIG);
                Pool.createPoolLedgerConfig(POOL_NAME, POOL_CONFIG);
                pool = Pool.openPoolLedger(POOL_NAME, "{}").get();
            } catch (IndyException | ExecutionException | InterruptedException e) {
                throw new ServerException(ExceptionStatus.INDY_INITIALIZATION_ERROR);
            }
        }

        log.info("Hyperledger Indy Pool created and opened successfully.");
        return pool;
    }

    public void createWallet(String config, String password) {
        String credential = this.walletConfigFactory.getWalletCredential(password);

        try {
            Wallet.createWallet(config, credential).get();
        } catch (InterruptedException | ExecutionException | IndyException exception) {
            exception.printStackTrace();
            throw new ServerException(ExceptionStatus.WALLET_CREATION_ERROR);
        }
    }

    public Wallet openWallet(String config, String password) {
        String credential = this.walletConfigFactory.getWalletCredential(password);

        try {
            Wallet wallet = Wallet.openWallet(config, credential).get();
            log.info("지갑 오픈 완료 | " + config);
            return wallet;
        } catch (InterruptedException | ExecutionException | IndyException exception) {
            exception.printStackTrace();
            throw new ServerException(ExceptionStatus.WALLET_OPEN_ERROR);
        }
    }
}
