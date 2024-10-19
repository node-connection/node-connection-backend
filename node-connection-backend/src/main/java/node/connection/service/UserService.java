package node.connection.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.client.BadRequestException;
import node.connection._core.exception.server.ServerException;
import node.connection._core.security.CustomUserDetails;
import node.connection._core.utils.AccessControl;
import node.connection._core.utils.Mapper;
import node.connection.data.BuildingDescription;
import node.connection.data.RegistryDocument;
import node.connection.data.IssuerData;
import node.connection.data.RegistryDocumentBuilder;
import node.connection.dto.registry.RegistryDocumentDto;
import node.connection.dto.user.request.IssuanceRequest;
import node.connection.dto.user.request.JoinDTO;
import node.connection.dto.user.response.IssuanceHistoryDto;
import node.connection.entity.Court;
import node.connection.entity.IssuanceHistory;
import node.connection.entity.UserAccount;
import node.connection.entity.constant.Role;
import node.connection.entity.pk.IssuanceHistoryKey;
import node.connection.hyperledger.FabricConfig;
import node.connection.hyperledger.fabric.FabricConnector;
import node.connection.hyperledger.fabric.FabricProposalResponse;
import node.connection.hyperledger.fabric.ca.CAEnrollment;
import node.connection.repository.CourtRepository;
import node.connection.repository.IssuanceHistoryRepository;
import node.connection.repository.JurisdictionRepository;
import node.connection.repository.UserAccountRepository;
import org.hyperledger.fabric.sdk.Enrollment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserService {

    private final FabricService fabricService;

    private final FabricConfig fabricConfig;

    private final Mapper objectMapper;

    private final UserAccountRepository userAccountRepository;

    private final CourtRepository courtRepository;

    private final IssuanceHistoryRepository issuanceHistoryRepository;

    private final JurisdictionRepository jurisdictionRepository;

    private final AccessControl accessControl;

    private final PasswordEncoder passwordEncoder;

    private final RegistryDocumentBuilder documentBuilder;


    public UserService(@Autowired FabricService fabricService,
                       @Autowired FabricConfig fabricConfig,
                       @Autowired Mapper objectMapper,
                       @Autowired UserAccountRepository userAccountRepository,
                       @Autowired CourtRepository courtRepository,
                       @Autowired IssuanceHistoryRepository issuanceHistoryRepository,
                       @Autowired JurisdictionRepository jurisdictionRepository,
                       @Autowired AccessControl accessControl,
                       @Autowired PasswordEncoder passwordEncoder,
                       @Autowired RegistryDocumentBuilder documentBuilder
    ) {
        this.fabricService = fabricService;
        this.fabricConfig = fabricConfig;
        this.objectMapper = objectMapper;
        this.userAccountRepository = userAccountRepository;
        this.courtRepository = courtRepository;
        this.issuanceHistoryRepository = issuanceHistoryRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.accessControl = accessControl;
        this.passwordEncoder = passwordEncoder;
        this.documentBuilder = documentBuilder;
    }

    @Transactional
    public void register(CustomUserDetails userDetails, JoinDTO joinDTO) {
        this.accessControl.hasAnonymousRole(userDetails);

        UserAccount userAccount = userDetails.getUserAccount();
        String id = userAccount.getFabricId();
        String msp = userAccount.getMspId();
        Court court = null;

        if (this.userAccountRepository.existsByFabricId(id)) {
            throw new BadRequestException(ExceptionStatus.ALREADY_REGISTERED);
        }

        if (joinDTO.courtCode() != null) {
            court = this.courtRepository.findByRegisterCode(joinDTO.courtCode())
                    .orElseThrow(() -> new BadRequestException(ExceptionStatus.INVALID_COURT_CODE));
        }

        Enrollment e = this.fabricService.register(userDetails);
        CAEnrollment caEnrollment = CAEnrollment.of(e);
        String enrollment = caEnrollment.serialize(objectMapper);
        userAccount.setEnrollment(enrollment);

        if (msp.equals(FabricService.VIEWER_MSP)) {
            userAccount.setRole(Role.VIEWER);
        }
        else if (msp.equals(FabricService.REGISTRY_MSP)) {
            userAccount.setRole(Role.REGISTRY);
        }

        String encodedSecret = this.passwordEncoder.encode(userAccount.getSecret());
        userAccount.setSecret(encodedSecret);
        userAccount.setUserName(joinDTO.username());
        userAccount.setPhoneNumber(joinDTO.phoneNumber());
        userAccount.setEmail(joinDTO.email());
        userAccount.setCourt(court);

        this.userAccountRepository.save(userAccount);
    }

    public void login(CustomUserDetails userDetails) {
        this.accessControl.hasAnonymousRole(userDetails);

        UserAccount userAccount = userDetails.getUserAccount();
        String id = userAccount.getFabricId();

        UserAccount _userAccount = this.userAccountRepository.findByFabricId(id)
                .orElseThrow(() -> new BadRequestException(ExceptionStatus.USER_NOT_FOUND));

        if (!passwordEncoder.matches(userAccount.getSecret(), _userAccount.getSecret())) {
            throw new BadRequestException(ExceptionStatus.INVALID_PASSWORD);
        }

        this.fabricService.enroll(userAccount.getMspId(), userAccount.getFabricId(), userAccount.getSecret());
    }

    @Transactional
    public String issuance(CustomUserDetails userDetails, IssuanceRequest request) {
        this.accessControl.hasMemberRole(userDetails);

        UserAccount userAccount = userDetails.getUserAccount();
        String id = userAccount.getFabricId();

        Court court = this.jurisdictionRepository.findCourtByAddress(request.address())
                .orElseThrow(() -> new BadRequestException(ExceptionStatus.NOT_SUPPORT_LOCATION));

        FabricConnector connector = this.fabricService.getConnectorByIdAndChannel(id, court.getChannelName());

        connector.setChaincode(FabricConfig.REGISTRY_CHAIN_CODE, this.fabricConfig.getRegistryChainCodeVersion());

        List<String> params = List.of(request.address(), request.detailAddress() == null ? "" : request.detailAddress());
        FabricProposalResponse response = connector.query("GetRegistryDocumentByAddress", params);

        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_QUERY_ERROR);
        }

        String payload = response.getPayload();
        List<RegistryDocumentDto> registryDocument = this.objectMapper.readValue(payload, new TypeReference<List<RegistryDocumentDto>>() {});
        String documentId = registryDocument.get(0).id();

        IssuerData issuerData = new IssuerData(
                userAccount.getFabricId(),
                userAccount.getUserName(),
                userAccount.getPhoneNumber(),
                userAccount.getEmail()
        );

        params = List.of(
                this.objectMapper.writeValueAsString(issuerData),
                documentId
        );

        connector.setChaincode(FabricConfig.ISSUANCE_CHAIN_CODE, this.fabricConfig.getIssuanceChainCodeVersion());
        response = connector.invoke("Issuance", params);
        if (!response.getSuccess()) {
            throw new ServerException(ExceptionStatus.FABRIC_INVOKE_ERROR);
        }

        String issuanceHash = response.getPayload();

        IssuanceHistoryKey key = IssuanceHistoryKey.builder()
                .address(request.address())
                .detailAddress(request.detailAddress())
                .issuanceHash(issuanceHash)
                .build();

        IssuanceHistory issuanceHistory = IssuanceHistory.builder()
                .key(key)
                .userAccount(userAccount)
                .registryDocumentId(documentId)
                .expiredAt(LocalDateTime.now().plusDays(90))
                .build();

        this.issuanceHistoryRepository.save(issuanceHistory);

        return issuanceHash;
    }

    public List<IssuanceHistoryDto> getIssuanceHistories(CustomUserDetails userDetails) {
        this.accessControl.hasMemberRole(userDetails);

        UserAccount userAccount = userDetails.getUserAccount();
        List<IssuanceHistory> histories = this.issuanceHistoryRepository.findAllByUserAccount(userAccount);
        return histories.stream()
                .map(history -> new IssuanceHistoryDto(
                        history.getKey().getIssuanceHash(),
                        history.getKey().getAddress(),
                        history.getKey().getDetailAddress(),
                        history.getCreatedAt().plusHours(9),
                        history.getExpiredAt().plusHours(9)
                ))
                .toList();
    }
}
