package node.connection.controller;

import node.connection._core.response.Response;
import node.connection._core.security.CustomUserDetails;
import node.connection.data.IssuerData;
import node.connection.dto.registry.*;
import node.connection.service.CourtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/court")
public class CourtController {

    private final CourtService courtService;

    public CourtController(@Autowired CourtService courtService) {
        this.courtService = courtService;
    }

    @PostMapping("/registry")
    public ResponseEntity<?> createRegistryDocument(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @RequestBody RegistryDocumentDto request
    ) {
        this.courtService.createRegistryDocument(userDetails, request);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PatchMapping("/{id}/title/building-description")
    public ResponseEntity<?> addBuildingDescriptionToTitleSection(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                  @PathVariable("id") String documentId,
                                                                  @RequestBody BuildingDescriptionDto data
    ) {
        this.courtService.addBuildingDescriptionToTitleSection(userDetails, documentId, data);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PatchMapping("/{id}/title/land-description")
    public ResponseEntity<?> addLandDescriptionToTitleSection(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @PathVariable("id") String documentId,
                                                              @RequestBody LandDescriptionDto data
    ) {
        this.courtService.addLandDescriptionToTitleSection(userDetails, documentId, data);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PatchMapping("/{id}/exclusive/building-description")
    public ResponseEntity<?> addBuildingDescriptionToExclusivePart(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                   @PathVariable("id") String documentId,
                                                                   @RequestBody BuildingPartDescriptionDto data
    ) {
        this.courtService.addBuildingDescriptionToExclusivePart(userDetails, documentId, data);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PatchMapping("/{id}/exclusive/land-rights")
    public ResponseEntity<?> addLandRightDescriptionToExclusivePart(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                    @PathVariable("id") String documentId,
                                                                    @RequestBody LandRightDescriptionDto data
    ) {
        this.courtService.addLandRightDescriptionToExclusivePart(userDetails, documentId, data);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PatchMapping("/{id}/first")
    public ResponseEntity<?> addFirstSectionEntry(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @PathVariable("id") String documentId,
                                                  @RequestBody FirstSectionDto data
    ) {
        this.courtService.addFirstSectionEntry(userDetails, documentId, data);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PatchMapping("/{id}/second")
    public ResponseEntity<?> addSecondSectionEntry(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @PathVariable("id") String documentId,
                                                   @RequestBody SecondSectionDto data
    ) {
        this.courtService.addSecondSectionEntry(userDetails, documentId, data);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @GetMapping("/issuer")
    public ResponseEntity<?> getIssuerDataByHash(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @RequestParam("hash") String hash
    ) {
        IssuerData issuerData = this.courtService.getIssuerDataByHash(userDetails, hash);
        return ResponseEntity.ok().body(Response.success(issuerData));
    }
}