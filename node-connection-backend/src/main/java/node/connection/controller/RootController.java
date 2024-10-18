package node.connection.controller;

import node.connection._core.response.Response;
import node.connection._core.security.CustomUserDetails;
import node.connection.dto.root.request.CourtCreateRequest;
import node.connection.service.CourtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/root")
public class RootController {

    private final CourtService courtService;


    public RootController(@Autowired CourtService courtService) {
        this.courtService = courtService;
    }

    @PostMapping("/court")
    public ResponseEntity<?> createNewCourt(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestBody CourtCreateRequest request
    ) {
        this.courtService.createCourt(userDetails, request);
        return ResponseEntity.ok().body(Response.success(null));
    }
}
