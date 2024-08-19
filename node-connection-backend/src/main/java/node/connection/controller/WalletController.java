package node.connection.controller;

import node.connection._core.response.Response;
import node.connection.dto.wallet.CourtWalletCreateRequest;
import node.connection.dto.wallet.OpenWalletRequest;
import node.connection.dto.wallet.UserWalletCreateRequest;
import node.connection.service.WalletService;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
public class WalletController {
    private final WalletService walletService;

    public WalletController(@Autowired WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/user")
    public ResponseEntity<?> createUserWallet(@RequestBody UserWalletCreateRequest walletCreateRequest) {
        walletService.createUserWallet(walletCreateRequest);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PostMapping("/user/open/{phoneNumber}")
    public ResponseEntity<?> openUserWallet(
            @PathVariable("phoneNumber") String phoneNumber,
            @RequestBody OpenWalletRequest request
    ) {
        String password = request.password();
        Wallet wallet = walletService.openUserWallet(phoneNumber, password);
        return ResponseEntity.ok().body(Response.success(wallet));
    }

    @PostMapping("/court")
    public ResponseEntity<?> createCourtWallet(@RequestBody CourtWalletCreateRequest request) {
        walletService.createCourtWallet(request);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @GetMapping("/court/open/{court}/{department}/{location}")
    public ResponseEntity<?> openCourtWallet(
            @PathVariable("court") String court,
            @PathVariable("department") String department,
            @PathVariable("location") String location,
            @RequestBody OpenWalletRequest request
    ) {
        String password = request.password();
        Wallet wallet = walletService.openCourtWallet(court, department, location, password);
        return ResponseEntity.ok().body(Response.success(wallet));
    }
}
