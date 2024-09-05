package node.connection.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection._core.security.CustomUserDetails;
import node.connection._core.utils.Mapper;
import node.connection.dto.court.response.FabricCourtRequest;
import node.connection.hyperledger.FabricConfig;
import node.connection.hyperledger.fabric.FabricProposalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserService {

    private final FabricService fabricService;

    private final FabricConfig fabricConfig;

    private final Mapper objectMapper;


    public UserService(
            @Autowired FabricService fabricService,
            @Autowired FabricConfig fabricConfig,
            @Autowired Mapper objectMapper
    ) {
        this.fabricService = fabricService;
        this.fabricConfig = fabricConfig;
        this.objectMapper = objectMapper;
    }

    public List<FabricCourtRequest> findRequestsByUser(CustomUserDetails userDetails) {
        this.fabricService.setChaincode("court", this.fabricConfig.getCourtChainCodeVersion());

        List<String> params = List.of(userDetails.getUsername());
        FabricProposalResponse response = this.fabricService.query("GetRequestsByRequestorId", params);

        if (!response.getSuccess()) {
            log.error("get court request error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }

        if (response.getPayload().isEmpty()) {
            return List.of();
        }

        return this.objectMapper.readValue(response.getPayload(), new TypeReference<List<FabricCourtRequest>>() {});
    }
}
