package node.connection.controller;

import node.connection._core.response.Response;
import node.connection._core.security.CustomUserDetails;
import node.connection.dto.root.response.FabricCourtRequest;
import node.connection.dto.user.request.IssuanceRequest;
import node.connection.dto.user.request.JoinDTO;
import node.connection.dto.user.response.IssuanceHistoryDto;
import node.connection.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;


    public UserController(
            @Autowired UserService userService
    ) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestBody JoinDTO joinDTO
    ) {
        this.userService.register(userDetails, joinDTO);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@AuthenticationPrincipal CustomUserDetails userDetails) {
        this.userService.login(userDetails);
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PostMapping("/issuance")
    public ResponseEntity<?> issuance(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestBody IssuanceRequest request
                                      ) {
        String issuanceHash = this.userService.issuance(userDetails, request);
        return ResponseEntity.ok().body(Response.success(issuanceHash));
    }

    @GetMapping("/issuance")
    public ResponseEntity<?> getIssuanceHistories(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<IssuanceHistoryDto> historyDtos = this.userService.getIssuanceHistories(userDetails);
        return ResponseEntity.ok().body(Response.success(historyDtos));
    }
}
