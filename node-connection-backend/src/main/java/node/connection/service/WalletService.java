package node.connection.service;

import node.connection.dto.wallet.CourtWalletCreateRequest;
import node.connection.dto.wallet.UserWalletCreateRequest;
import node.connection.hyperledger.indy.IndyConnector;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final IndyConnector indyConnector;

    public WalletService(@Autowired IndyConnector indyConnector) {
        this.indyConnector = indyConnector;
    }

    public void createUserWallet(UserWalletCreateRequest request) {
        String phoneNumber = request.phoneNumber();
        String password = request.password();
        indyConnector.createUserWallet(phoneNumber, password);
    }

    public Wallet openUserWallet(String phoneNumber, String password) {
        return indyConnector.openUserWallet(phoneNumber, password);
    }

    public void createCourtWallet(CourtWalletCreateRequest request) {
        String court = request.court();
        String department = request.department();
        String location = request.location();
        String password = request.password();
        indyConnector.createCourtWallet(court, department, location, password);
    }

    public Wallet openCourtWallet(String court, String department, String location, String password) {
        return indyConnector.openCourtWallet(court, department, location, password);
    }
}
