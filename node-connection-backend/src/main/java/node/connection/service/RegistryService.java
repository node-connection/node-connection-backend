package node.connection.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.server.ServerException;
import node.connection.data.registry.RegistryDocument;
import node.connection.hyperledger.FabricService;
import node.connection.hyperledger.fabric.FabricProposalResponse;
import node.connection.hyperledger.indy.IndyConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RegistryService {

    private final IndyConnector indyConnector;

    private final FabricService fabricService;

    private final ObjectMapper objectMapper;

    public RegistryService(
            @Autowired IndyConnector indyConnector,
            @Autowired FabricService fabricService,
            @Autowired ObjectMapper objectMapper
    ) {
        this.indyConnector = indyConnector;
        this.fabricService = fabricService;
        this.objectMapper = objectMapper;
    }

    public void createRegistryDocument(RegistryDocument document) {
        try {
            String documentToJson = objectMapper.writeValueAsString(document);
            List<String> params = List.of(documentToJson);
            this.fabricService.setChaincode("registry", "1.0.0");
            FabricProposalResponse response = this.fabricService.invoke("CreateRegistryDocument", params);
            log.debug(String.valueOf(response));
        } catch (JsonProcessingException e) {
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        }
    }

    public RegistryDocument getRegistryDocumentById(String id) {
        List<String> params = List.of(id);
        this.fabricService.setChaincode("registry", "1.0.0");
        FabricProposalResponse response = this.fabricService.invoke("GetRegistryDocumentByID", params);
        log.debug(String.valueOf(response));

        String payload = response.getPayload();
        try {
            return objectMapper.readValue(payload, RegistryDocument.class);
        } catch (JsonProcessingException e) {
            throw new ServerException(ExceptionStatus.JSON_PROCESSING_EXCEPTION);
        }
    }
}
