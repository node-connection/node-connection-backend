package node.connection.service;

import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection._core.security.CustomUserDetails;
import node.connection._core.utils.AccessControl;
import node.connection._core.utils.Mapper;
import node.connection.data.RegistryDocument;
import node.connection.data.RegistryDocumentBuilder;
import node.connection.dto.registry.*;
import node.connection.dto.root.request.CourtCreateRequest;
import node.connection.entity.Court;
import node.connection.entity.Jurisdiction;
import node.connection.entity.RegistryDocumentIndex;
import node.connection.entity.UserAccount;
import node.connection.entity.pk.AddressIndexKey;
import node.connection.hyperledger.FabricConfig;
import node.connection.hyperledger.fabric.FabricConnector;
import node.connection.hyperledger.fabric.FabricProposalResponse;
import node.connection.hyperledger.fabric.NetworkConfig;
import node.connection.repository.CourtRepository;
import node.connection.repository.JurisdictionRepository;
import node.connection.repository.RegistryDocumentIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CourtService {

    private final FabricService fabricService;

    private final FabricConfig fabricConfig;

    private final CourtRepository courtRepository;

    private final JurisdictionRepository jurisdictionRepository;

    private final RegistryDocumentIndexRepository registryDocumentIndexRepository;

    private final RegistryDocumentBuilder documentBuilder;

    private final Mapper objectMapper;

    private final AccessControl accessControl;


    public CourtService(
            @Autowired FabricService fabricService,
            @Autowired FabricConfig fabricConfig,
            @Autowired CourtRepository courtRepository,
            @Autowired JurisdictionRepository jurisdictionRepository,
            @Autowired RegistryDocumentIndexRepository registryDocumentIndexRepository,
            @Autowired RegistryDocumentBuilder documentBuilder,
            @Autowired Mapper objectMapper,
            @Autowired AccessControl accessControl
    ) {
        this.fabricService = fabricService;
        this.fabricConfig = fabricConfig;
        this.courtRepository = courtRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.registryDocumentIndexRepository = registryDocumentIndexRepository;
        this.documentBuilder = documentBuilder;
        this.objectMapper = objectMapper;
        this.accessControl = accessControl;
    }

    @Transactional
    public void createCourt(CustomUserDetails userDetails, CourtCreateRequest request) {
        this.accessControl.hasRootRole(userDetails);

        Court court = Court.of(request);
        this.courtRepository.save(court);

        List<Jurisdiction> jurisdictions = new ArrayList<>();
        request.getJurisdictions().forEach(jurisdiction -> jurisdictions.add(Jurisdiction.of(jurisdiction, court)));
        this.jurisdictionRepository.saveAll(jurisdictions);

        FabricConnector connector = this.fabricService.getRootFabricConnector();
        NetworkConfig networkConfig = this.fabricService.getNetworkConfig();
        networkConfig.setChannelName(request.getChannelName());
        connector.connectToChannel(networkConfig);
    }

    @Transactional
    public void createRegistryDocument(CustomUserDetails userDetails, RegistryDocumentDto document) {
        FabricConnector connector = this.getFabricConnector(userDetails);
        RegistryDocument registryDocument = this.documentBuilder.build("", document);
        String documentJson = this.objectMapper.writeValueAsString(registryDocument);

        FabricProposalResponse response = connector.invoke("CreateRegistryDocument", List.of(documentJson));
        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }

        String documentId = response.getPayload();

        String address = document.address();
        String detailAddress = document.detailAddress();

        AddressIndexKey key = AddressIndexKey.builder()
                .address(address)
                .detailAddress(detailAddress)
                .build();

        RegistryDocumentIndex index = RegistryDocumentIndex.builder()
                .key(key)
                .documentId(documentId)
                .build();

        this.registryDocumentIndexRepository.save(index);
    }

    public void addBuildingDescriptionToTitleSection(CustomUserDetails userDetails,
                                                     String documentId,
                                                     BuildingDescriptionDto data
    ) {
        FabricConnector connector = this.getFabricConnector(userDetails);

        String payload = this.objectMapper.writeValueAsString(data);
        List<String> params = List.of(documentId, payload);

        FabricProposalResponse response = connector.invoke("AddBuildingDescriptionToTitleSection", params);
        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    public void addLandDescriptionToTitleSection(CustomUserDetails userDetails,
                                                 String documentId,
                                                 LandDescriptionDto data
    ) {
        FabricConnector connector = this.getFabricConnector(userDetails);

        String payload = this.objectMapper.writeValueAsString(data);
        List<String> params = List.of(documentId, payload);

        FabricProposalResponse response = connector.invoke("AddLandDescriptionToTitleSection", params);
        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    public void addBuildingDescriptionToExclusivePart(CustomUserDetails userDetails,
                                                      String documentId,
                                                      BuildingPartDescriptionDto data
    ) {
        FabricConnector connector = this.getFabricConnector(userDetails);

        String payload = this.objectMapper.writeValueAsString(data);
        List<String> params = List.of(documentId, payload);

        FabricProposalResponse response = connector.invoke("AddBuildingDescriptionToExclusivePart", params);
        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    public void addLandRightDescriptionToExclusivePart(CustomUserDetails userDetails,
                                                       String documentId,
                                                       LandRightDescriptionDto data
    ) {
        FabricConnector connector = this.getFabricConnector(userDetails);

        String payload = this.objectMapper.writeValueAsString(data);
        List<String> params = List.of(documentId, payload);

        FabricProposalResponse response = connector.invoke("AddLandRightDescriptionToExclusivePart", params);
        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    public void addFirstSectionEntry(CustomUserDetails userDetails,
                                     String documentId,
                                     FirstSectionDto data
    ) {
        FabricConnector connector = this.getFabricConnector(userDetails);

        String payload = this.objectMapper.writeValueAsString(data);
        List<String> params = List.of(documentId, payload);

        FabricProposalResponse response = connector.invoke("AddFirstSectionEntry", params);
        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    public void addSecondSectionEntry(CustomUserDetails userDetails,
                                      String documentId,
                                      SecondSectionDto data
    ) {
        FabricConnector connector = this.getFabricConnector(userDetails);

        String payload = this.objectMapper.writeValueAsString(data);
        List<String> params = List.of(documentId, payload);

        FabricProposalResponse response = connector.invoke("AddSecondSectionEntry", params);
        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }
    }

    private FabricConnector getFabricConnector(CustomUserDetails userDetails) {
        this.accessControl.hasRegistryRole(userDetails);

        UserAccount userAccount = userDetails.getUserAccount();
        String id = userAccount.getFabricId();
        FabricConnector connector = this.fabricService.getConnectorById(id);
        connector.setChaincode(FabricConfig.REGISTRY_CHAIN_CODE, this.fabricConfig.getRegistryChainCodeVersion());
        return connector;
    }
}
