package main

import (
	"encoding/json"
	"errors"
	"fmt"
	"github.com/hyperledger/fabric-contract-api-go/contractapi"
	"time"
)

type SmartContract struct {
	contractapi.Contract
	GlobalIndex *CourtRequestGlobalIndex
}

type CourtRequestGlobalIndex struct {
	CourtByRequestID           map[string]string        // 요청 ID -> 법원 ID
	RequestsByRequester        map[string][]string      // 요청자 ID -> 요청 ID 리스트
	RequestsByDocumentID       map[string][]string      // Document ID -> 요청 ID 리스트
}

type Court struct {
	ID                   		string               			`json:"id"`
	Court                		string               			`json:"court"`
	Support              		string               			`json:"support"`
	Office               		string               			`json:"office"`
	Owner                		string               			`json:"owner"`
	Members              		[]string             			`json:"members"`
	RequestsByID            map[string]*CourtRequest 	`json:"requestsByID"`
	FinalizedRequestsByID   map[string]*CourtRequest 	`json:"finalizedRequestsByID"`
	UnfinalizedRequestsByID map[string]*CourtRequest 	`json:"unfinalizedRequestsByID"`
}

type CourtRequest struct {
	ID            string          `json:"id"`
	DocumentID    string          `json:"documentId"`
	Action        string          `json:"action"`
	Payload       string          `json:"payload"`
	Finalized     bool            `json:"finalized"`
	RequestDate   string          `json:"requestDate"`
	RequestedBy   string          `json:"requestedBy,omitempty"`
	FinalizeDate  string          `json:"finalizeDate,omitempty"`
	FinalizedBy   string          `json:"finalizedBy,omitempty"`
	Status        string          `json:"status"`
	ErrorMessage  string          `json:"errorMessage,omitempty"`
	ForwardedTo   string          `json:"forwardedTo,omitempty"`
	ForwardedFrom string 				  `json:"forwardedFrom,omitempty"`
}

const GlobalIndexKey = "GLOBAL_INDEX"

func (s *SmartContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	err := s.InitializeSmartContract(ctx)
	if err != nil {
			return err
	}
	return nil
}

func (s *SmartContract) InitializeSmartContract(ctx contractapi.TransactionContextInterface) error {
	globalIndex, err := s.loadGlobalIndex(ctx)
	if err != nil {
			return err
	}
	s.GlobalIndex = globalIndex
	return nil
}

func (s *SmartContract) saveGlobalIndex(ctx contractapi.TransactionContextInterface, globalIndex *CourtRequestGlobalIndex) error {
	globalIndexJSON, err := json.Marshal(globalIndex)
	if err != nil {
		return fmt.Errorf("failed to marshal global index: %v", err)
	}

	err = ctx.GetStub().PutState(GlobalIndexKey, globalIndexJSON)
	if err != nil {
		return fmt.Errorf("failed to save global index: %v", err)
	}

	return nil
}

func (s *SmartContract) loadGlobalIndex(ctx contractapi.TransactionContextInterface) (*CourtRequestGlobalIndex, error) {
	globalIndexJSON, err := ctx.GetStub().GetState(GlobalIndexKey)
	if err != nil {
		return nil, fmt.Errorf("failed to load global index: %v", err)
	}

	if globalIndexJSON == nil {
		return &CourtRequestGlobalIndex{
			CourtByRequestID:    make(map[string]string),
			RequestsByRequester: make(map[string][]string),
			RequestsByDocumentID: make(map[string][]string),
		}, nil
	}

	var globalIndex CourtRequestGlobalIndex
	err = json.Unmarshal(globalIndexJSON, &globalIndex)
	if err != nil {
		return nil, fmt.Errorf("failed to unmarshal global index: %v", err)
	}

	return &globalIndex, nil
}

func (s *SmartContract) CreateCourt(ctx contractapi.TransactionContextInterface, id, court, support, office, owner string) (*Court, error) {
	globalIndex, err := s.loadGlobalIndex(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to load global index: %v", err)
	}

	courts, err := s.GetAllCourts(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to get courts: %v", err)
	}

	for _, existingCourt := range courts {
		if existingCourt.Court == court && existingCourt.Support == support && existingCourt.Office == office {
			return nil, errors.New("a court with the same court, support, and office already exists")
		}
	}

	newCourt := &Court{
		ID:                     id,
		Court:                  court,
		Support:                support,
		Office:                 office,
		Owner:                  owner,
		Members:                []string{},
		RequestsByID:           make(map[string]*CourtRequest),
		FinalizedRequestsByID:  make(map[string]*CourtRequest),
		UnfinalizedRequestsByID: make(map[string]*CourtRequest),
	}

	courtJSON, err := json.Marshal(newCourt)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal court: %v", err)
	}

	err = ctx.GetStub().PutState(id, courtJSON)
	if err != nil {
		return nil, fmt.Errorf("failed to put state: %v", err)
	}

	if globalIndex.CourtByRequestID == nil {
		globalIndex.CourtByRequestID = make(map[string]string)
	}
	if globalIndex.RequestsByRequester == nil {
		globalIndex.RequestsByRequester = make(map[string][]string)
	}
	if globalIndex.RequestsByDocumentID == nil {
		globalIndex.RequestsByDocumentID = make(map[string][]string)
	}

	err = s.saveGlobalIndex(ctx, globalIndex)
	if err != nil {
		return nil, fmt.Errorf("failed to save global index: %v", err)
	}

	return newCourt, nil
}

func (s *SmartContract) GetCourtByID(ctx contractapi.TransactionContextInterface, id string) (*Court, error) {
	courtJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}
	if courtJSON == nil {
		return nil, fmt.Errorf("court %s does not exist", id)
	}

	var court Court
	err = json.Unmarshal(courtJSON, &court)
	if err != nil {
		return nil, fmt.Errorf("failed to unmarshal court JSON: %v", err)
	}

	return &court, nil
}

func (s *SmartContract) GetAllCourts(ctx contractapi.TransactionContextInterface) ([]*Court, error) {
	resultsIterator, err := ctx.GetStub().GetStateByRange("", "")
	if err != nil {
		return nil, fmt.Errorf("failed to get state by range: %v", err)
	}
	defer resultsIterator.Close()

	var courts []*Court
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, fmt.Errorf("failed to get next result: %v", err)
		}

		var court Court
		err = json.Unmarshal(queryResponse.Value, &court)
		if err != nil {
			return nil, fmt.Errorf("failed to unmarshal court JSON: %v", err)
		}

		courts = append(courts, &court)
	}

	return courts, nil
}

func (s *SmartContract) AddMember(ctx contractapi.TransactionContextInterface, courtID string, memberID string) error {
	court, err := s.GetCourtByID(ctx, courtID)
	if err != nil {
		return err
	}

	clientID, err := s.getClientID(ctx)
	if err != nil {
		return err
	}

	if court.Owner != clientID {
		return errors.New("only the owner can add members")
	}

	for _, member := range court.Members {
		if member == memberID {
			return errors.New("member already exists in the court")
		}
	}

	court.Members = append(court.Members, memberID)

	courtJSON, err := json.Marshal(court)
	if err != nil {
		return fmt.Errorf("failed to marshal court JSON: %v", err)
	}

	err = ctx.GetStub().PutState(courtID, courtJSON)
	if err != nil {
		return fmt.Errorf("failed to put court: %v", err)
	}

	return nil
}

func (s *SmartContract) UpdateOwner(ctx contractapi.TransactionContextInterface, courtID string, newOwnerID string) error {
	court, err := s.GetCourtByID(ctx, courtID)
	if err != nil {
		return err
	}

	clientID, err := s.getClientID(ctx)
	if err != nil {
		return err
	}

	if court.Owner != clientID {
		return errors.New("only the current owner can update the owner")
	}

	court.Owner = newOwnerID

	courtJSON, err := json.Marshal(court)
	if err != nil {
		return fmt.Errorf("failed to marshal court JSON: %v", err)
	}

	err = ctx.GetStub().PutState(courtID, courtJSON)
	if err != nil {
		return fmt.Errorf("failed to put court: %v", err)
	}

	return nil
}

func (s *SmartContract) RemoveMember(ctx contractapi.TransactionContextInterface, courtID string, memberID string) error {
	court, err := s.GetCourtByID(ctx, courtID)
	if err != nil {
		return err
	}

	clientID, err := s.getClientID(ctx)
	if err != nil {
		return err
	}

	if court.Owner != clientID {
		return errors.New("only the owner can remove members")
	}

	memberExists := false
	for _, member := range court.Members {
		if member == memberID {
			memberExists = true
			break
		}
	}

	if !memberExists {
		return errors.New("member does not exist in the court")
	}

	newMembers := []string{}
	for _, member := range court.Members {
		if member != memberID {
			newMembers = append(newMembers, member)
		}
	}
	court.Members = newMembers

	courtJSON, err := json.Marshal(court)
	if err != nil {
		return fmt.Errorf("failed to marshal court JSON: %v", err)
	}

	err = ctx.GetStub().PutState(courtID, courtJSON)
	if err != nil {
		return fmt.Errorf("failed to put court: %v", err)
	}

	return nil
}

func (s *SmartContract) AddRequest(ctx contractapi.TransactionContextInterface, courtID string, request CourtRequest) error {
	globalIndex, err := s.loadGlobalIndex(ctx)
	if err != nil {
		return fmt.Errorf("failed to load global index: %v", err)
	}

	court, err := s.GetCourtByID(ctx, courtID)
	if err != nil {
		return err
	}

	clientID, err := s.getClientID(ctx)
	if err != nil {
		return err
	}

	if court.Owner != clientID && !contains(court.Members, clientID) {
		return errors.New("only the owner or members can add requests")
	}

	if request.ID == "" {
		return errors.New("request ID is required")
	}
	if request.DocumentID == "" {
		return errors.New("request DocumentID is required")
	}
	if request.Action == "" {
		return errors.New("request Action is required")
	}
	if request.Payload == "" {
		return errors.New("request Payload is required")
	}
	if request.RequestDate == "" {
		return errors.New("request RequestDate is required")
	}

	request.Status = "Pending"
	request.FinalizeDate = "-"
	request.ErrorMessage = "-"
	request.ForwardedTo = "-"
	request.ForwardedFrom = "-"

	request.RequestedBy = clientID
	request.Finalized = false
	request.FinalizedBy = "-"

	if _, exists := globalIndex.CourtByRequestID[request.ID]; exists {
		return errors.New("a request with the same ID already exists")
	}
	globalIndex.CourtByRequestID[request.ID] = courtID

	if _, exists := globalIndex.RequestsByRequester[request.RequestedBy]; !exists {
		globalIndex.RequestsByRequester[request.RequestedBy] = []string{}
	}
	globalIndex.RequestsByRequester[request.RequestedBy] = append(globalIndex.RequestsByRequester[request.RequestedBy], request.ID)

	if _, exists := globalIndex.RequestsByDocumentID[request.DocumentID]; !exists {
		globalIndex.RequestsByDocumentID[request.DocumentID] = []string{}
	}
	globalIndex.RequestsByDocumentID[request.DocumentID] = append(globalIndex.RequestsByDocumentID[request.DocumentID], request.ID)

	if _, exists := court.UnfinalizedRequestsByID[request.ID]; exists {
		return errors.New("a request with the same ID already exists")
	}

	court.RequestsByID[request.ID] = &request
	court.UnfinalizedRequestsByID[request.ID] = &request

	courtJSON, err := json.Marshal(court)
	if err != nil {
		return err
	}

	err = ctx.GetStub().PutState(courtID, courtJSON)
	if err != nil {
		return err
	}

	if err := s.saveGlobalIndex(ctx, globalIndex); err != nil {
		return fmt.Errorf("failed to save global index: %v", err)
	}

	return nil
}

func (s *SmartContract) FinalizeRequest(ctx contractapi.TransactionContextInterface, courtID string, requestID string, status string, errorMessage string) error {
	court, err := s.GetCourtByID(ctx, courtID)
	if err != nil {
		return err
	}

	clientID, err := s.getClientID(ctx)
	if err != nil {
		return err
	}

	if court.Owner != clientID && !contains(court.Members, clientID) {
		return errors.New("only the owner or members can finalize requests")
	}

	request, exists := court.RequestsByID[requestID]
	if !exists {
		return fmt.Errorf("request with ID %s does not exist in court %s", requestID, courtID)
	}

	if request.Finalized {
		return errors.New("request is already finalized")
	}

	if status == "success" {
		chaincodeName := "registry"
		actionArgs := [][]byte{[]byte(request.Action), []byte(request.DocumentID)}

		if len(request.Payload) > 0 {
			actionArgs = append(actionArgs, []byte(request.Payload))
		}

		response := ctx.GetStub().InvokeChaincode(chaincodeName, actionArgs, "")
		if response.Status != 200 {
			return fmt.Errorf("failed to invoke chaincode: %s", response.Message)
		}

		request.Status = "Success"
		request.ErrorMessage = "-"
	} else {
		request.Status = "Fail"
		request.ErrorMessage = errorMessage
	}

	loc, err := time.LoadLocation("Asia/Seoul")
	if err != nil {
			return fmt.Errorf("failed to load location: %v", err)
	}
	request.Finalized = true
	request.FinalizeDate = time.Now().In(loc).Format(time.RFC3339)
	request.FinalizedBy = clientID

	delete(court.UnfinalizedRequestsByID, requestID)
	court.FinalizedRequestsByID[requestID] = request

	globalIndex, err := s.loadGlobalIndex(ctx)
	if err != nil {
		return fmt.Errorf("failed to load global index: %v", err)
	}

	globalIndex.CourtByRequestID[requestID] = courtID

	err = s.saveGlobalIndex(ctx, globalIndex)
	if err != nil {
		return fmt.Errorf("failed to save global index: %v", err)
	}

	courtJSON, err := json.Marshal(court)
	if err != nil {
		return fmt.Errorf("failed to marshal court JSON: %v", err)
	}
	err = ctx.GetStub().PutState(courtID, courtJSON)
	if err != nil {
		return fmt.Errorf("failed to put court: %v", err)
	}

	eventName := "RequestFinalized"
	eventPayload, err := json.Marshal(request)
	if err != nil {
		return fmt.Errorf("failed to marshal event payload: %v", err)
	}
	err = ctx.GetStub().SetEvent(eventName, eventPayload)
	if err != nil {
		return fmt.Errorf("failed to set event: %v", err)
	}

	return nil
}

func (s *SmartContract) ForwardRequest(ctx contractapi.TransactionContextInterface, requestID string, targetCourtID string) error {
	clientID, err := s.getClientID(ctx)
	if err != nil {
		return err
	}

	currentCourt, err := s.GetCourtByID(ctx, s.GlobalIndex.CourtByRequestID[requestID])
	if err != nil {
		return fmt.Errorf("failed to get current court: %v", err)
	}

	if currentCourt.Owner != clientID && !contains(currentCourt.Members, clientID) {
		return errors.New("only the owner or members can forward requests")
	}

	request, exists := currentCourt.RequestsByID[requestID]
	if !exists {
		return fmt.Errorf("request with ID %s does not exist in current court %s", requestID, currentCourt.ID)
	}

	if request.Finalized {
		return errors.New("request is already finalized")
	}

	targetCourt, err := s.GetCourtByID(ctx, targetCourtID)
	if err != nil {
		return fmt.Errorf("failed to get target court: %v", err)
	}

	if _, exists := targetCourt.RequestsByID[requestID]; exists {
		return fmt.Errorf("request with ID %s already exists in target court %s", requestID, targetCourtID)
	}

	loc, err := time.LoadLocation("Asia/Seoul")
	if err != nil {
			return fmt.Errorf("failed to load location: %v", err)
	}

	request.Finalized = true
	request.FinalizeDate = time.Now().In(loc).Format(time.RFC3339)
	request.Status = "Forwarded"
	request.ForwardedTo = targetCourtID
	request.FinalizedBy = clientID

	delete(currentCourt.UnfinalizedRequestsByID, requestID)
	currentCourt.FinalizedRequestsByID[requestID] = request

	newRequest := *request
	newRequest.Finalized = false
	newRequest.FinalizeDate = "-"
	newRequest.FinalizedBy = "-"
	newRequest.Status = "Pending"
	newRequest.ForwardedFrom = currentCourt.ID
	targetCourt.RequestsByID[requestID] = &newRequest
	targetCourt.UnfinalizedRequestsByID[requestID] = &newRequest
	
	globalIndex, err := s.loadGlobalIndex(ctx)
	if err != nil {
			return fmt.Errorf("failed to load global index: %v", err)
	}

	if _, exists := globalIndex.RequestsByDocumentID[newRequest.DocumentID]; !exists {
		globalIndex.RequestsByDocumentID[newRequest.DocumentID] = []string{}
	}
	globalIndex.RequestsByDocumentID[newRequest.DocumentID] = append(globalIndex.RequestsByDocumentID[newRequest.DocumentID], requestID)

	globalIndex.CourtByRequestID[requestID] = targetCourtID

	globalIndexJSON, err := json.Marshal(globalIndex)
	if err != nil {
		return fmt.Errorf("failed to marshal global index JSON: %v", err)
	}
	err = ctx.GetStub().PutState(GlobalIndexKey, globalIndexJSON)
	if err != nil {
		return fmt.Errorf("failed to update global index: %v", err)
	}

	currentCourtJSON, err := json.Marshal(currentCourt)
	if err != nil {
		return fmt.Errorf("failed to marshal current court JSON: %v", err)
	}
	err = ctx.GetStub().PutState(currentCourt.ID, currentCourtJSON)
	if err != nil {
		return fmt.Errorf("failed to put state for current court: %v", err)
	}

	targetCourtJSON, err := json.Marshal(targetCourt)
	if err != nil {
		return fmt.Errorf("failed to marshal target court JSON: %v", err)
	}
	err = ctx.GetStub().PutState(targetCourtID, targetCourtJSON)
	if err != nil {
		return fmt.Errorf("failed to put state for target court: %v", err)
	}

	return nil
}

func (s *SmartContract) GetAllUnfinalizedRequests(ctx contractapi.TransactionContextInterface, courtID string) ([]CourtRequest, error) {
	court, err := s.GetCourtByID(ctx, courtID)
	if err != nil {
		return nil, err
	}

	var unfinalizedRequests []CourtRequest
	for _, request := range court.UnfinalizedRequestsByID {
		unfinalizedRequests = append(unfinalizedRequests, *request)
	}

	return unfinalizedRequests, nil
}

func (s *SmartContract) GetAllFinalizedRequests(ctx contractapi.TransactionContextInterface, courtID string) ([]CourtRequest, error) {
	court, err := s.GetCourtByID(ctx, courtID)
	if err != nil {
		return nil, err
	}

	var finalizedRequests []CourtRequest
	for _, request := range court.FinalizedRequestsByID {
		finalizedRequests = append(finalizedRequests, *request)
	}

	return finalizedRequests, nil
}

func (s *SmartContract) GetRequestsByRequestorId(ctx contractapi.TransactionContextInterface, requestorID string) ([]CourtRequest, error) {
	globalIndex, err := s.loadGlobalIndex(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to load global index: %v", err)
	}

	requestIDs, exists := globalIndex.RequestsByRequester[requestorID]
	if !exists || len(requestIDs) == 0 {
		return nil, fmt.Errorf("no requests found for requester ID %s", requestorID)
	}

	var requests []CourtRequest

	for _, requestID := range requestIDs {
		courtID, exists := globalIndex.CourtByRequestID[requestID]
		if !exists {
			continue
		}

		court, err := s.GetCourtByID(ctx, courtID)
		if err != nil {
			return nil, fmt.Errorf("failed to get court by ID %s: %v", courtID, err)
		}

		request, exists := court.RequestsByID[requestID]
		if !exists {
			continue
		}

		requests = append(requests, *request)
	}

	if len(requests) == 0 {
		return nil, fmt.Errorf("no requests found for requester ID %s", requestorID)
	}

	return requests, nil
}

func contains(slice []string, item string) bool {
	for _, v := range slice {
		if v == item {
			return true
		}
	}
	return false
}

func (s *SmartContract) getClientID(ctx contractapi.TransactionContextInterface) (string, error) {
	certBase64, err := ctx.GetClientIdentity().GetX509Certificate()
	if err != nil {
		return "", fmt.Errorf("failed to get client certificate: %v", err)
	}

	return certBase64.Subject.CommonName, nil
}

func main() {
	chaincode, err := contractapi.NewChaincode(&SmartContract{})
	if err != nil {
		fmt.Printf("Error creating chaincode: %v\n", err)
		return
	}

	if err := chaincode.Start(); err != nil {
		fmt.Printf("Error starting chaincode: %v\n", err)
	}
}