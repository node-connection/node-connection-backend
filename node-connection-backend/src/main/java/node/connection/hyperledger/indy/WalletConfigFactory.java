package node.connection.hyperledger.indy;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WalletConfigFactory {

    @Value("${hyperledger.indy.wallet.user-wallet-storage}")
    private String userWalletStorage;

    @Value(("${hyperledger.indy.wallet.court-wallet-storage}"))
    private String courtWalletStorage;

    public WalletConfigFactory() {}

    public String getUserWalletConfig(String phoneNumber) {
        JSONObject storageConfig = new JSONObject().put("path", this.userWalletStorage);

        return new JSONObject()
                .put("id", phoneNumber)
                .put("storage_config", storageConfig)
                .toString();
    }

    public String getCourtWalletConfig(String court, String department, String location) {
        JSONObject storageConfig = new JSONObject().put("path", this.courtWalletStorage);

        return new JSONObject()
                .put("id", this.getCourtId(court, department, location))
                .put("storage_config", storageConfig)
                .toString();
    }

    public String getWalletCredential(String password) {
        return new JSONObject()
                .put("key", password)
                .toString();
    }

    public String getCourtId(String court, String department, String location) {
        return court + "_" + department + "_" + location;
    }
}
