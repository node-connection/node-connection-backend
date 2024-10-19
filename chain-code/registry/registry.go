/*
SPDX-License-Identifier: Apache-2.0
*/

package main

import (
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"fmt"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

type SmartContract struct {
	contractapi.Contract
}

// 부동산 등기부등본 (Registry Document)
type RegistryDocument struct {
	ID                       string                     `json:"id"`                       // 등기부등본 ID
	Address									 string											`json:"address"`									// 주소
	DetailAddress						 string 										`json:"detailAddress"`					  // 상세주소
	TitleSection             TitleSection               `json:"titleSection"`             // 표제부
	ExclusivePartDescription ExclusivePartDescription   `json:"exclusivePartDescription"` // 전유부분의 건물의 표시
	FirstSection             []FirstSection             `json:"firstSection"`             // 갑구
	SecondSection            []SecondSection            `json:"secondSection"`            // 을구
}

// 표제부 (Title Section)
type TitleSection struct {
	BuildingDescription      []BuildingDescription      `json:"buildingDescription"`      // 건물의 표시
	LandDescription          []LandDescription          `json:"landDescription"`          // 대지권의 목적인 토지의 표시
}

type BuildingDescription struct {
	DisplayNumber            string    `json:"displayNumber"`            // 표시번호
	ReceiptDate              string    `json:"receiptDate"`              // 접수
	LocationNumber           string    `json:"locationNumber"`           // 소재지번, 건물명칭 및 번호
	BuildingDetails          string    `json:"buildingDetails"`          // 건물내역
	RegistrationCause        string    `json:"registrationCause"`        // 등기원인 및 기타사항
}

type LandDescription struct {
	DisplayNumber            string    `json:"displayNumber"`            // 표시번호
	LocationNumber           string    `json:"locationNumber"`           // 소재지번, 건물명칭 및 번호
	LandType                 string    `json:"landType"`                 // 지목
	Area                     string    `json:"area"`                     // 면적
	RegistrationCause        string    `json:"registrationCause"`        // 등기원인 및 기타사항
}

// 전유부분의 건물의 표시 (Exclusive Part of the Building)
type ExclusivePartDescription struct {
	BuildingPartDescription  []BuildingPartDescription  `json:"buildingPartDescription"`  // 전유부분의 건물의 표시
	LandRightDescription     []LandRightDescription     `json:"landRightDescription"` // 대지권의 표시
}

type BuildingPartDescription struct {
	DisplayNumber            string    `json:"displayNumber"`            // 표시번호
	ReceiptDate              string    `json:"receiptDate"`              // 접수
	PartNumber               string    `json:"partNumber"`               // 건물번호
	BuildingDetails          string    `json:"buildingDetails"`          // 건물내역
	RegistrationCause        string    `json:"registrationCause"`        // 등기원인 및 기타사항
}

type LandRightDescription struct {
	DisplayNumber            string    `json:"displayNumber"`            // 표시번호
	LandRightType            string    `json:"landRightType"`            // 대지권의 종류
	LandRightRatio           string    `json:"landRightRatio"`           // 대지권 비율
	RegistrationCause        string    `json:"registrationCause"`        // 등기원인 및 기타사항
}

// 갑구 (First Section)
type FirstSection struct {
	RankNumber               string    `json:"rankNumber"`               // 순위번호
	RegistrationPurpose      string    `json:"registrationPurpose"`      // 등기목적
	ReceiptDate              string    `json:"receiptDate"`              // 접수
	RegistrationCause        string    `json:"registrationCause"`        // 등기원인
	HolderAndAdditionalInfo  string    `json:"holderAndAdditionalInfo"`  // 권리자 및 기타사항
}

// 을구 (Second Section)
type SecondSection struct {
	RankNumber               string    `json:"rankNumber"`               // 순위번호
	RegistrationPurpose      string    `json:"registrationPurpose"`      // 등기목적
	ReceiptDate              string    `json:"receiptDate"`              // 접수
	RegistrationCause        string    `json:"registrationCause"`        // 등기원인
	HolderAndAdditionalInfo  string    `json:"holderAndAdditionalInfo"`  // 권리자 및 기타사항
}

// fabric 초기화
func (s *SmartContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	return nil
}

func generateDocumentID(payload string) string {
	hash := sha256.New()
	hash.Write([]byte(payload))
	return base64.RawURLEncoding.EncodeToString(hash.Sum(nil))
}

// 등기부등본 생성
func (s *SmartContract) CreateRegistryDocument(ctx contractapi.TransactionContextInterface, document RegistryDocument) (string, error) {
	if isRegistryMSP, err := isRegistryMSP(ctx); !isRegistryMSP {
		return "", fmt.Errorf("only RegistryMSP can create a document: %v", err)
	}

	documentJSON, err := json.Marshal(document)
	if err != nil {
		return "", fmt.Errorf("failed to marshal document: %v", err)
	}

	id := generateDocumentID(string(documentJSON))
	document.ID = id

	documentJSON, err = json.Marshal(document)
	if err != nil {
		return "", fmt.Errorf("failed to marshal document: %v", err)
	}

	err = ctx.GetStub().PutState(id, documentJSON)
	if err != nil {
		return id, fmt.Errorf("failed to put document into world state: %v", err)
	}

	return id, err
}

func (s *SmartContract) GetRegistryDocumentByID(ctx contractapi.TransactionContextInterface, id string) (*RegistryDocument, error) {
	documentJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}

	if documentJSON == nil {
		return nil, fmt.Errorf("the document %s does not exist", id)
	}

	var document RegistryDocument
	if err := json.Unmarshal(documentJSON, &document); err != nil {
		return nil, fmt.Errorf("failed to unmarshal document JSON: %v", err)
	}

	return &document, nil
}

func (s *SmartContract) GetRegistryDocumentByAddress(ctx contractapi.TransactionContextInterface, address string, detailAddress string) ([]*RegistryDocument, error) {
	queryString := fmt.Sprintf(`{"selector":{"address": "%s", "detailAddress": "%s"}}`, address, detailAddress)
	resultsIterator, err := ctx.GetStub().GetQueryResult(queryString)
	if err != nil {
		return nil, fmt.Errorf("failed to execute query: %v", err)
	}
	defer resultsIterator.Close()

	var documents []*RegistryDocument

	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, fmt.Errorf("failed to retrieve next item: %v", err)
		}

		var document RegistryDocument
		if err := json.Unmarshal(queryResponse.Value, &document); err != nil {
			return nil, fmt.Errorf("failed to unmarshal document JSON: %v", err)
		}

		documents = append(documents, &document)
	}

	return documents, nil
}

func (s *SmartContract) AddBuildingDescriptionToTitleSection(ctx contractapi.TransactionContextInterface, id string, buildingDesc BuildingDescription) error {
	if isRegistryMSP, err := isRegistryMSP(ctx); !isRegistryMSP {
		return err
	}

	document, err := s.GetRegistryDocumentByID(ctx, id)
	if err != nil {
		return err
	}

	document.TitleSection.BuildingDescription = append(document.TitleSection.BuildingDescription, buildingDesc)
	documentJSON, err := json.Marshal(document)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, documentJSON)
}

func (s *SmartContract) AddLandDescriptionToTitleSection(ctx contractapi.TransactionContextInterface, id string, landDesc LandDescription) error {
	if isRegistryMSP, err := isRegistryMSP(ctx); !isRegistryMSP {
		return err
	}

	document, err := s.GetRegistryDocumentByID(ctx, id)
	if err != nil {
		return err
	}

	document.TitleSection.LandDescription = append(document.TitleSection.LandDescription, landDesc)
	documentJSON, err := json.Marshal(document)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, documentJSON)
}

func (s *SmartContract) AddBuildingDescriptionToExclusivePart(ctx contractapi.TransactionContextInterface, id string, buildingDesc BuildingPartDescription) error {
	if isRegistryMSP, err := isRegistryMSP(ctx); !isRegistryMSP {
		return err
	}

	document, err := s.GetRegistryDocumentByID(ctx, id)
	if err != nil {
		return err
	}

	document.ExclusivePartDescription.BuildingPartDescription = append(document.ExclusivePartDescription.BuildingPartDescription, buildingDesc)
	documentJSON, err := json.Marshal(document)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, documentJSON)
}

func (s *SmartContract) AddLandRightDescriptionToExclusivePart(ctx contractapi.TransactionContextInterface, id string, landRightDesc LandRightDescription) error {
	if isRegistryMSP, err := isRegistryMSP(ctx); !isRegistryMSP {
		return err
	}

	document, err := s.GetRegistryDocumentByID(ctx, id)
	if err != nil {
		return err
	}

	document.ExclusivePartDescription.LandRightDescription = append(document.ExclusivePartDescription.LandRightDescription, landRightDesc)
	documentJSON, err := json.Marshal(document)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, documentJSON)
}

func (s *SmartContract) AddFirstSectionEntry(ctx contractapi.TransactionContextInterface, id string, firstSectionEntry FirstSection) error {
	if isRegistryMSP, err := isRegistryMSP(ctx); !isRegistryMSP {
		return err
	}

	document, err := s.GetRegistryDocumentByID(ctx, id)
	if err != nil {
		return err
	}

	document.FirstSection = append(document.FirstSection, firstSectionEntry)
	documentJSON, err := json.Marshal(document)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, documentJSON)
}

func (s *SmartContract) AddSecondSectionEntry(ctx contractapi.TransactionContextInterface, id string, secondSectionEntry SecondSection) error {
	if isRegistryMSP, err := isRegistryMSP(ctx); !isRegistryMSP {
		return err
	}

	document, err := s.GetRegistryDocumentByID(ctx, id)
	if err != nil {
		return err
	}

	document.SecondSection = append(document.SecondSection, secondSectionEntry)
	documentJSON, err := json.Marshal(document)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, documentJSON)
}

func isRegistryMSP(ctx contractapi.TransactionContextInterface) (bool, error) {
	mspid, err := ctx.GetClientIdentity().GetMSPID()
	if err != nil {
		return false, fmt.Errorf("failed to get MSPID: %v", err)
	}

	return mspid == "RegistryMSP", nil
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