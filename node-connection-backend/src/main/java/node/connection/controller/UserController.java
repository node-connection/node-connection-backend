package node.connection.controller;

import node.connection._core.response.Response;
import node.connection._core.security.CustomUserDetails;
import node.connection.dto.court.response.FabricCourtRequest;
import node.connection.service.FabricService;
import node.connection.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    private final FabricService fabricService;

    public UserController(
            @Autowired UserService userService,
            @Autowired FabricService fabricService
    ) {
        this.userService = userService;
        this.fabricService = fabricService;
    }

    @GetMapping("/request")
    public ResponseEntity<?> findRequestsByUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<FabricCourtRequest> requests = this.userService.findRequestsByUser(userDetails);
        return ResponseEntity.ok().body(Response.success(requests));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@AuthenticationPrincipal CustomUserDetails userDetails) {
        this.fabricService.register(userDetails);
        return ResponseEntity.ok().body(Response.success(null));
    }
}
