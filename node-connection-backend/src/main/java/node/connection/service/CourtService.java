package node.connection.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection._core.security.CustomUserDetails;
import node.connection._core.utils.Mapper;
import node.connection.data.court.CourtRequest;
import node.connection.data.registry.RegistryBuilder;
import node.connection.data.registry.RegistryDocument;
import node.connection.dto.court.request.AddCourtMemberRequest;
import node.connection.dto.court.request.CourtCreateRequest;
import node.connection.dto.court.request.DeleteCourtMemberRequest;
import node.connection.dto.court.request.FinalizeCourtRequest;
import node.connection.dto.court.response.FabricCourt;
import node.connection.dto.court.response.FabricCourtRequest;
import node.connection.dto.registry.RegistryDocumentDto;
import node.connection.dto.registry.request.*;
import node.connection.entity.Court;
import node.connection.entity.Jurisdiction;
import node.connection.hyperledger.FabricConfig;
import node.connection.hyperledger.fabric.FabricConnector;
import node.connection.hyperledger.fabric.FabricProposalResponse;
import node.connection.repository.CourtRepository;
import node.connection.repository.JurisdictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CourtService {

    private final FabricService fabricService;

    private final FabricConfig fabricConfig;

    private final CourtRepository courtRepository;

    private final JurisdictionRepository jurisdictionRepository;

    private final Mapper objectMapper;

    private final RegistryBuilder registryBuilder;


    public CourtService(
            @Autowired FabricService fabricService,
            @Autowired FabricConfig fabricConfig,
            @Autowired CourtRepository courtRepository,
            @Autowired JurisdictionRepository jurisdictionRepository,
            @Autowired Mapper objectMapper,
            @Autowired RegistryBuilder registryBuilder
    ) {
        this.fabricService = fabricService;
        this.fabricConfig = fabricConfig;
        this.courtRepository = courtRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.objectMapper = objectMapper;
        this.registryBuilder = registryBuilder;
    }

    @Transactional
    public void createCourt(CustomUserDetails userDetails, CourtCreateRequest request) {
        String courtId = request.getCourtId();

        Court court = Court.of(request);
        this.courtRepository.save(court);

        List<Jurisdiction> jurisdictions = new ArrayList<>();
        request.getJurisdictions().forEach(jurisdiction -> {
            jurisdictions.add(Jurisdiction.of(jurisdiction, court));
        });
        this.jurisdictionRepository.saveAll(jurisdictions);

        FabricConnector connector = this.fabricService.getConnectorById(userDetails.getUsername());
        connector.setChaincode("court", this.fabricConfig.getCourtChainCodeVersion());

        List<String> params = List.of(
                courtId,
                request.getCourt(),
                request.getSupport(),
                request.getOffice(),
                userDetails.getUsername()
        );
        FabricProposalResponse response = connector.invoke("CreateCourt", params);

        if (!response.getSuccess()) {
            log.error("court creation error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    public FabricCourt getCourtById(String id) {
        this.fabricService.setChaincode("court", this.fabricConfig.getCourtChainCodeVersion());
        FabricProposalResponse response = this.fabricService.query("GetCourtByID", List.of(id));

        if (!response.getSuccess()) {
            log.error("court creation error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }

        return this.objectMapper.readValue(response.getPayload(), FabricCourt.class);
    }

    public void addCourtMember(CustomUserDetails userDetails, AddCourtMemberRequest request) {
        FabricConnector connector = this.fabricService.getConnectorById(userDetails.getUsername());
        connector.setChaincode("court", this.fabricConfig.getCourtChainCodeVersion());

        List<String> params = List.of(
                request.getCourtId(),
                request.getMemberId()
        );
        FabricProposalResponse response = connector.invoke("AddMember", params);

        if (!response.getSuccess()) {
            log.error("add court member error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    public void deleteCourtMember(CustomUserDetails userDetails, DeleteCourtMemberRequest request) {
        String courtId = request.getCourtId();
        FabricConnector connector = this.fabricService.getConnectorById(userDetails.getUsername());
        connector.setChaincode("court", this.fabricConfig.getCourtChainCodeVersion());

        List<String> params = List.of(
                courtId,
                request.getMemberId()
        );
        FabricProposalResponse response = connector.invoke("RemoveMember", params);

        if (!response.getSuccess()) {
            log.error("add court member error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    public List<FabricCourtRequest> getUnfinalizedRequests(String id) {
        this.fabricService.setChaincode("court", this.fabricConfig.getCourtChainCodeVersion());

        List<String> params = List.of(id);
        FabricProposalResponse response = this.fabricService.query("GetAllUnfinalizedRequests", params);

        if (!response.getSuccess()) {
            log.error("get unfinalized court request error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }

        if (response.getPayload().isEmpty()) {
            return List.of();
        }

        return this.objectMapper.readValue(response.getPayload(), new TypeReference<List<FabricCourtRequest>>() {});
    }

    public List<FabricCourtRequest> getFinalizedRequests(String id) {
        this.fabricService.setChaincode("court", this.fabricConfig.getCourtChainCodeVersion());

        List<String> params = List.of(id);
        FabricProposalResponse response = this.fabricService.query("GetAllFinalizedRequests", params);

        if (!response.getSuccess()) {
            log.error("get finalized court request error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }

        if (response.getPayload().isEmpty()) {
            return List.of();
        }

        return this.objectMapper.readValue(response.getPayload(), new TypeReference<List<FabricCourtRequest>>() {});
    }

    public void createRegistryCourtRequest(CustomUserDetails userDetails, String courtId, RegistryDocumentDto document) {
        String requestId = this.createId();
        String documentId = this.createId();
        RegistryDocument registryDocument = this.registryBuilder.build(documentId, document);
        String documentJson = this.objectMapper.writeValueAsString(registryDocument);

        CourtRequest courtRequest = CourtRequest.builder()
                .id(requestId)
                .documentId(documentId)
                .action("CreateRegistryDocument")
                .payload(documentJson)
                .build();

        this.invokeAddRequest(userDetails.getUsername(), courtId, courtRequest);
    }

    public void addBuildingDescriptionToTitleSection(
            CustomUserDetails userDetails,
            String courtId,
            AddBuildingDescriptionToTitleSection data
    ) {
        String requestId = this.createId();
        String payload = this.objectMapper.writeValueAsString(data.buildingDescription());

        CourtRequest courtRequest = CourtRequest.builder()
                .id(requestId)
                .documentId(data.documentId())
                .action("AddBuildingDescriptionToTitleSection")
                .payload(payload)
                .build();

        this.invokeAddRequest(userDetails.getUsername(), courtId, courtRequest);
    }

    public void addLandDescriptionToTitleSection(
            CustomUserDetails userDetails,
            String courtId,
            AddLandDescriptionToTitleSection data
    ) {
        String requestId = this.createId();
        String payload = this.objectMapper.writeValueAsString(data.landDescription());

        CourtRequest courtRequest = CourtRequest.builder()
                .id(requestId)
                .documentId(data.documentId())
                .action("AddBuildingDescriptionToTitleSection")
                .payload(payload)
                .build();

        this.invokeAddRequest(userDetails.getUsername(), courtId, courtRequest);
    }

    public void addBuildingDescriptionToExclusivePart(
            CustomUserDetails userDetails,
            String courtId,
            AddBuildingPartDescriptionToExclusivePart data
    ) {
        String requestId = this.createId();
        String payload = this.objectMapper.writeValueAsString(data.buildingPartDescription());

        CourtRequest courtRequest = CourtRequest.builder()
                .id(requestId)
                .documentId(data.documentId())
                .action("AddBuildingDescriptionToExclusivePart")
                .payload(payload)
                .build();

        this.invokeAddRequest(userDetails.getUsername(), courtId, courtRequest);
    }

    public void addLandRightDescriptionToExclusivePart(
            CustomUserDetails userDetails,
            String courtId,
            AddLandRightDescriptionToExclusivePart data
    ) {
        String requestId = this.createId();
        String payload = this.objectMapper.writeValueAsString(data.landRightDescription());

        CourtRequest courtRequest = CourtRequest.builder()
                .id(requestId)
                .documentId(data.documentId())
                .action("AddLandRightDescriptionToExclusivePart")
                .payload(payload)
                .build();

        this.invokeAddRequest(userDetails.getUsername(), courtId, courtRequest);
    }

    public void addFirstSectionEntry(
            CustomUserDetails userDetails,
            String courtId,
            AddFirstSectionEntry data
    ) {
        String requestId = this.createId();
        String payload = this.objectMapper.writeValueAsString(data.firstSection());

        CourtRequest courtRequest = CourtRequest.builder()
                .id(requestId)
                .documentId(data.documentId())
                .action("AddFirstSectionEntry")
                .payload(payload)
                .build();

        this.invokeAddRequest(userDetails.getUsername(), courtId, courtRequest);
    }

    public void addSecondSectionEntry(
            CustomUserDetails userDetails,
            String courtId,
            AddSecondSectionEntry data
    ) {
        String requestId = this.createId();
        String payload = this.objectMapper.writeValueAsString(data.secondSection());

        CourtRequest courtRequest = CourtRequest.builder()
                .id(requestId)
                .documentId(data.documentId())
                .action("AddFirstSectionEntry")
                .payload(payload)
                .build();

        this.invokeAddRequest(userDetails.getUsername(), courtId, courtRequest);
    }

    private void invokeAddRequest(String user, String courtId, CourtRequest request) {
        FabricConnector connector = this.fabricService.getConnectorById(user);
        connector.setChaincode("court", this.fabricConfig.getCourtChainCodeVersion());

        String requestJson = this.objectMapper.writeValueAsString(request);
        List<String> params = List.of(courtId, requestJson);
        FabricProposalResponse response = connector.invoke("AddRequest", params);

        if (!response.getSuccess()) {
            log.error("add court request error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    public void finalizeCourtRequest(CustomUserDetails userDetails, String courtId, FinalizeCourtRequest request) {
        FabricConnector connector = this.fabricService.getConnectorById(userDetails.getUsername());
        connector.setChaincode("court", this.fabricConfig.getCourtChainCodeVersion());

        List<String> params = List.of(
                courtId,
                request.requestId(),
                request.status(),
                request.errorMessage()
        );
        FabricProposalResponse response = connector.invoke("FinalizeRequest", params);

        if (!response.getSuccess()) {
            log.error("finalize court request error: {}, payload: {}", response.getMessage(), response.getPayload());
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    private String createId() {
        return String.valueOf(UUID.randomUUID());
    }
}
