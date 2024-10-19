/*
SPDX-License-Identifier: Apache-2.0
*/

package main

import (
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"time"

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

type IssuerData struct {
	ID                       string    `json:"id"`                       // 요청자 ID
	Name                     string    `json:"name"`                     // 요청자 이름
	PhoneNumber              string    `json:"phoneNumber"`              // 요청자 전화번호
	Email                    string    `json:"email"`                    // 요청자 이메일
}

type IssuanceData struct {
	TxId                    string						`json:"txId"`                   // 트랜잭션 ID
	IssuerName              string    				`json:"issuerName"`             // 발급자 이름
  IssuerDataHash 					string  					`json:"issuerDataHash"`  				// 요청자 정보 해시값
	RegistryDocument			 	RegistryDocument	`json:"registryDocument"`  			// 등기부등본 정보 해시값
	IssuanceDate						string						`json:"issuanceDate"`						// 발급일
	ExpirationDate					string						`json:"expirationDate"`					// 만료일
}

const HASH_SALT = "qwer1234"

func (s *SmartContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	return nil
}

/*
* 등기부등본 발급
* @param 	issuerData 요청자 정보
* @param 	registryDocument 등기부등본 정보
* @return 발급된 등기부등본의 해시값
*/
func (s *SmartContract) Issuance(ctx contractapi.TransactionContextInterface, issuerData IssuerData, registryDocumentID string) (string, error) {
	/*
	* 1. 요청자 정보 PDC 저장
	* 2. 등기부등본 정보 해싱
	* 3. 발급 내역 저장
	*/

	// 1. 요청자 정보 PDC 저장
	cert, err := ctx.GetClientIdentity().GetX509Certificate()
	if err != nil {
		return "", fmt.Errorf("failed to get invoker ID: %v", err)
	}

	invokerID := cert.Subject.CommonName

	if issuerData.ID != invokerID {
		return "", fmt.Errorf("the invoker ID %s does not match the issuer ID %s", invokerID, issuerData.ID)
	}

	data, _ := json.Marshal(issuerData)
	issuerHash := encodeIssuerData(data)
	err = ctx.GetStub().PutPrivateData("IssuerInfoCollection", issuerHash, data)
	if err != nil {
		return "", fmt.Errorf("failed to put private data: %s", err)
	}

	// 2. 등기부등본 정보 조회
	response := ctx.GetStub().InvokeChaincode("registry", [][]byte{[]byte("GetRegistryDocumentByID"), []byte(registryDocumentID)}, "")
	if response.Status != 200 {
		return "", fmt.Errorf("failed to invoke chaincode: %s", response.Message)
	}

	var registryDocument RegistryDocument
	err = json.Unmarshal(response.Payload, &registryDocument)
	if err != nil {
		return "", fmt.Errorf("failed to unmarshal registry document: %s", err)
	}

	// 3. 발급 내역 저장
	// 3-1. 발급 날짜 및 만료 날짜 계산
	loc, err := time.LoadLocation("Asia/Seoul")
	if err != nil {
		return "", fmt.Errorf("failed to load location: %v", err)
	}

	issuanceDate := time.Now().In(loc).Format(time.DateTime)
	expirationDate := time.Now().In(loc).AddDate(0, 0, 90).Format(time.DateTime)

	// 3-2. 발급자 이름 마스킹
	var issuerName string
	issuerRunes := []rune(issuerData.Name)

	if len(issuerRunes) == 2 {
		issuerName = string(issuerRunes[0]) + "*"
	} else if len(issuerRunes) > 2 {
		issuerName = string(issuerRunes[0])
		for i := 0; i < len(issuerRunes) - 2; i++ {
			issuerName += "*"
		}
		issuerName += string(issuerRunes[len(issuerRunes) - 1])
	}

	// 3-3. 발급 내역 저장
	issuanceData := IssuanceData{
		TxId: ctx.GetStub().GetTxID(),
		IssuerName: issuerName,
		RegistryDocument: registryDocument,
		IssuerDataHash: issuerHash,
		IssuanceDate: issuanceDate,
		ExpirationDate: expirationDate,
	}

	issuanceDataJSON, err := json.Marshal(issuanceData)
	if err != nil {
		return "", fmt.Errorf("failed to marshal PDC data: %s", err)
	}

	issuanceHash := generateIssuanceHash(issuanceDataJSON)

	err = ctx.GetStub().PutState(issuanceHash, issuanceDataJSON)
	if err != nil {
		return "", fmt.Errorf("failed to put state: %s", err)
	}

	// event 발생
	err = ctx.GetStub().SetEvent("issuance", []byte(issuanceHash))
	if err != nil {
		return "", fmt.Errorf("failed to set event: %s", err)
	}

	return issuanceHash, nil
}

func encodeIssuerData(data []byte) string {
	saltedData := string(data) + HASH_SALT
	hash := sha256.Sum256([]byte(saltedData))
	return base64.RawURLEncoding.EncodeToString(hash[:])
}

func generateIssuanceHash(data []byte) string {
	saltedData := string(data) + HASH_SALT
	hash := sha256.Sum256([]byte(saltedData))
	return base64.RawURLEncoding.EncodeToString(hash[:])
}

func (s *SmartContract) GetIssuanceDataByHash(ctx contractapi.TransactionContextInterface, issuanceHash string) (*IssuanceData, error) {
	issuanceDataJSON, err := ctx.GetStub().GetState(issuanceHash)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}

	if issuanceDataJSON == nil {
		return nil, fmt.Errorf("the issuance data %s does not exist", issuanceHash)
	}

	var issuanceData IssuanceData
	if err := json.Unmarshal(issuanceDataJSON, &issuanceData); err != nil {
		return nil, fmt.Errorf("failed to unmarshal issuance data JSON: %v", err)
	}

	loc, err := time.LoadLocation("Asia/Seoul")
	if err != nil {
		return nil, fmt.Errorf("failed to load location: %v", err)
	}

	now := time.Now().In(loc)
	expirationDate, err := time.Parse(time.DateTime, issuanceData.ExpirationDate)
	if err != nil {
		return nil, fmt.Errorf("failed to parse expiration date: %v", err)
	}

	if now.After(expirationDate) {
		return nil, fmt.Errorf("the issuance data %s has expired", issuanceHash)
	}

	return &issuanceData, nil
}

func (s *SmartContract) GetIssuerDataByIssuanceHash(ctx contractapi.TransactionContextInterface, issuanceHash string) (*IssuerData, error) {
	issuanceDataJSON, err := ctx.GetStub().GetState(issuanceHash)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}

	if issuanceDataJSON == nil {
		return nil, fmt.Errorf("the issuance data %s does not exist", issuanceHash)
	}

	var issuanceData IssuanceData
	if err := json.Unmarshal(issuanceDataJSON, &issuanceData); err != nil {
		return nil, fmt.Errorf("failed to unmarshal issuance data JSON: %v", err)
	}

	issuerDataJSON, err := ctx.GetStub().GetPrivateData("IssuerInfoCollection", issuanceData.IssuerDataHash)
	if err != nil {
		return nil, fmt.Errorf("failed to read from private data collection: %v", err)
	}

	if issuerDataJSON == nil {
		return nil, fmt.Errorf("the issuer data %s does not exist", issuanceData.IssuerDataHash)
	}

	var issuerData IssuerData
	if err := json.Unmarshal(issuerDataJSON, &issuerData); err != nil {
		return nil, fmt.Errorf("failed to unmarshal issuer data JSON: %v", err)
	}

	return &issuerData, nil
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