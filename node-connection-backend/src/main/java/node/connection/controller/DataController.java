package node.connection.controller;

import node.connection._core.response.Response;
import node.connection._core.security.CustomUserDetails;
import node.connection.service.ConfigDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/data")
public class DataController {

    private final ConfigDataService configDataService;


    public DataController(@Autowired ConfigDataService configDataService) {
        this.configDataService = configDataService;
    }

    @PatchMapping("/chain-code/court")
    public ResponseEntity<?> updateCourtChainCode(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @RequestBody String version
    ) {
        this.configDataService.updateIssuanceChainCodeVersion(userDetails, version);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PostMapping("/chain-code/registry")
    public ResponseEntity<?> updateRegistryChainCode(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                     @RequestBody String version
    ) {
        this.configDataService.updateRegistryChainCodeVersion(customUserDetails, version);
        return ResponseEntity.ok().body(Response.success(null));
    }
}
