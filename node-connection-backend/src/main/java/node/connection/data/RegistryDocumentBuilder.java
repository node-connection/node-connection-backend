package node.connection.data;

import node.connection.dto.registry.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegistryDocumentBuilder {

    public RegistryDocument build(String id, RegistryDocumentDto record) {
        return RegistryDocument.builder()
                .id(id)
                .address(record.address())
                .detailAddress(record.detailAddress())
                .titleSection(convertTitleSection(record.titleSectionDto()))
                .exclusivePartDescription(convertExclusivePartDescriptionList(record.exclusivePartDescriptionDto()))
                .firstSection(convertFirstSectionList(record.firstSectionDto()))
                .secondSection(convertSecondSectionList(record.secondSectionDto()))
                .build();
    }

    private TitleSection convertTitleSection(TitleSectionDto record) {
        return TitleSection.builder()
                .buildingDescription(convertBuildingDescriptionList(record.buildingDescriptionDto()))
                .landDescription(convertLandDescriptionList(record.landDescriptionDto()))
                .build();
    }

    private List<BuildingDescription> convertBuildingDescriptionList(List<BuildingDescriptionDto> records) {
        return records.stream()
                .map(record -> BuildingDescription.builder()
                        .displayNumber(record.displayNumber())
                        .receiptDate(record.receiptDate())
                        .locationNumber(record.locationNumber())
                        .buildingDetails(record.buildingDetails())
                        .registrationCause(record.registrationCause())
                        .build())
                .toList();
    }

    private List<LandDescription> convertLandDescriptionList(List<LandDescriptionDto> records) {
        return records.stream()
                .map(record -> LandDescription.builder()
                        .displayNumber(record.displayNumber())
                        .locationNumber(record.locationNumber())
                        .landType(record.landType())
                        .area(record.area())
                        .registrationCause(record.registrationCause())
                        .build())
                .toList();
    }

    private ExclusivePartDescription convertExclusivePartDescriptionList(ExclusivePartDescriptionDto record) {
        return ExclusivePartDescription.builder()
                .buildingPartDescription(convertBuildingPartDescriptionList(record.buildingPartDescriptionDto()))
                .landRightDescription(convertLandRightDescriptionList(record.landRightDescriptionDto()))
                .build();
    }

    private List<BuildingPartDescription> convertBuildingPartDescriptionList(List<BuildingPartDescriptionDto> records) {
        return records.stream()
                .map(record -> BuildingPartDescription.builder()
                        .displayNumber(record.displayNumber())
                        .receiptDate(record.receiptDate())
                        .partNumber(record.partNumber())
                        .buildingDetails(record.buildingDetails())
                        .registrationCause(record.registrationCause())
                        .build())
                .toList();
    }

    private List<LandRightDescription> convertLandRightDescriptionList(List<LandRightDescriptionDto> records) {
        return records.stream()
                .map(record -> LandRightDescription.builder()
                        .displayNumber(record.displayNumber())
                        .landRightType(record.landRightType())
                        .landRightRatio(record.landRightRatio())
                        .registrationCause(record.registrationCause())
                        .build())
                .toList();
    }

    private List<FirstSection> convertFirstSectionList(List<FirstSectionDto> records) {
        return records.stream()
                .map(record -> FirstSection.builder()
                        .rankNumber(record.rankNumber())
                        .registrationPurpose(record.registrationPurpose())
                        .receiptDate(record.receiptDate())
                        .registrationCause(record.registrationCause())
                        .holderAndAdditionalInfo(record.holderAndAdditionalInfo())
                        .build())
                .toList();
    }

    private List<SecondSection> convertSecondSectionList(List<SecondSectionDto> records) {
        return records.stream()
                .map(record -> SecondSection.builder()
                        .rankNumber(record.rankNumber())
                        .registrationPurpose(record.registrationPurpose())
                        .receiptDate(record.receiptDate())
                        .registrationCause(record.registrationCause())
                        .holderAndAdditionalInfo(record.holderAndAdditionalInfo())
                        .build())
                .toList();
    }
}