package node.connection.controller;

import node.connection._core.response.Response;
import node.connection.service.ConfigDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/data")
public class DataController {

    private final ConfigDataService configDataService;


    public DataController(@Autowired ConfigDataService configDataService) {
        this.configDataService = configDataService;
    }

    @PatchMapping("/chain-code/court")
    public ResponseEntity<?> updateCourtChainCode(@RequestBody String version) {
        this.configDataService.updateCourtChainCodeVersion(version);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PostMapping("/chain-code/registry")
    public ResponseEntity<?> updateRegistryChainCode(@RequestBody String version) {
        this.configDataService.updateRegistryChainCodeVersion(version);
        return ResponseEntity.ok().body(Response.success(null));
    }
}
