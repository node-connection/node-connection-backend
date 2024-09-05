package node.connection.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import node.connection._core.response.Response;
import node.connection._core.security.CustomUserDetails;
import node.connection.dto.registry.RegistryDocumentDto;
import node.connection.dto.registry.request.RegistryCreateRequest;
import node.connection.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registry")
public class RegistryController {

    private final RegistryService registryService;

    public RegistryController(@Autowired RegistryService registryService) {
        this.registryService = registryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRegistry(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("id") String id) {
        RegistryDocumentDto document = this.registryService.getRegistryDocumentById(userDetails, id);
        return ResponseEntity.ok().body(Response.success(document));
    }
}
