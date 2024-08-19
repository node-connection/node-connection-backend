package node.connection.controller;

import node.connection._core.response.Response;
import node.connection.data.registry.RegistryDocument;
import node.connection.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registry")
public class RegistryController {

    private final RegistryService registryService;

    public RegistryController(@Autowired RegistryService registryService) {
        this.registryService = registryService;
    }

    @PostMapping("")
    public ResponseEntity<?> createRegistry(@RequestBody RegistryDocument document) {
        this.registryService.createRegistryDocument(document);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRegistry(@PathVariable("id") String id) {
        RegistryDocument document = this.registryService.getRegistryDocumentById(id);
        return ResponseEntity.ok().body(Response.success(document));
    }
}
