package node.connection.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.client.BadRequestException;
import node.connection._core.exception.server.ServerException;
import node.connection._core.security.CustomUserDetails;
import node.connection._core.utils.AccessControl;
import node.connection._core.utils.Mapper;
import node.connection.data.IssuanceData;
import node.connection.dto.registry.RegistryDocumentDto;
import node.connection.dto.registry.response.RegistryDocumentByHashDto;
import node.connection.entity.Court;
import node.connection.entity.UserAccount;
import node.connection.hyperledger.FabricConfig;
import node.connection.hyperledger.fabric.FabricConnector;
import node.connection.hyperledger.fabric.FabricProposalResponse;
import node.connection.repository.JurisdictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RegistryService {

    private final FabricService fabricService;

    private final JurisdictionRepository jurisdictionRepository;

    private final AccessControl accessControl;

    private final FabricConfig fabricConfig;

    private final Mapper objectMapper;


    public RegistryService(@Autowired FabricService fabricService,
                           @Autowired JurisdictionRepository jurisdictionRepository,
                           @Autowired AccessControl accessControl,
                           @Autowired FabricConfig fabricConfig,
                           @Autowired Mapper objectMapper
    ) {
        this.fabricService = fabricService;
        this.jurisdictionRepository = jurisdictionRepository;
        this.accessControl = accessControl;
        this.fabricConfig = fabricConfig;
        this.objectMapper = objectMapper;
    }

    public List<RegistryDocumentDto> getRegistryDocumentByAddress(CustomUserDetails userDetails, String address, String detailAddress) {
        this.accessControl.hasMemberRole(userDetails);

        Court court = this.jurisdictionRepository.findCourtByAddress(address)
                .orElseThrow(() -> new BadRequestException(ExceptionStatus.NOT_SUPPORT_LOCATION));

        FabricConnector connector = this.fabricService.getConnectorByIdAndChannel(userDetails.getUsername(), court.getChannelName());
        connector.setChaincode(FabricConfig.REGISTRY_CHAIN_CODE, this.fabricConfig.getRegistryChainCodeVersion());

        List<String> params = List.of(address, detailAddress == null ? "" : detailAddress);
        FabricProposalResponse response = connector.query("GetRegistryDocumentByAddress", params);

        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_QUERY_ERROR);
        }

        String payload = response.getPayload();
        return this.objectMapper.readValue(payload, new TypeReference<List<RegistryDocumentDto>>() {});
    }

    public RegistryDocumentByHashDto getRegistryDocumentByHash(CustomUserDetails userDetails, String address, String hash) {
        this.accessControl.hasMemberRole(userDetails);

        UserAccount userAccount = userDetails.getUserAccount();

        Court court = this.jurisdictionRepository.findCourtByAddress(address)
                .orElseThrow(() -> new BadRequestException(ExceptionStatus.NOT_SUPPORT_LOCATION));

        String id = userAccount.getFabricId();
        FabricConnector connector = this.fabricService.getConnectorByIdAndChannel(id, court.getChannelName());
        connector.setChaincode(FabricConfig.ISSUANCE_CHAIN_CODE, this.fabricConfig.getIssuanceChainCodeVersion());

        FabricProposalResponse response = connector.query("GetIssuanceDataByHash", List.of(hash));
        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_QUERY_ERROR);
        }

        String payload = response.getPayload();
        IssuanceData issuanceData = this.objectMapper.readValue(payload, IssuanceData.class);

        String documentId = issuanceData.registryDocument().id();
        connector.setChaincode(FabricConfig.REGISTRY_CHAIN_CODE, this.fabricConfig.getRegistryChainCodeVersion());

        response = connector.query("GetRegistryDocumentByID", List.of(documentId));
        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_QUERY_ERROR);
        }

        payload = response.getPayload();
        RegistryDocumentDto latestDocument = this.objectMapper.readValue(payload, RegistryDocumentDto.class);

        return new RegistryDocumentByHashDto(
                issuanceData.txId(),
                issuanceData.issuerName(),
                issuanceData.issuanceDate(),
                issuanceData.expirationDate(),
                issuanceData.registryDocument(),
                latestDocument
        );
    }
}
