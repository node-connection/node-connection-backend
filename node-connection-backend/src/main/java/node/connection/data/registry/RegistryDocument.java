package node.connection.data.registry;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RegistryDocument {
    @JsonProperty("id")
    private String id;

    @JsonProperty("titleSection")
    private TitleSection titleSection;

    @JsonProperty("exclusivePartDescription")
    private ExclusivePartDescription exclusivePartDescription;

    @JsonProperty("firstSection")
    private List<FirstSection> firstSection;

    @JsonProperty("secondSection")
    private List<SecondSection> secondSection;
}

@Getter
@Builder
class TitleSection {
    @JsonProperty("buildingDescription")
    private List<BuildingDescription> buildingDescription;

    @JsonProperty("landDescription")
    private List<LandDescription> landDescription;
}

@Getter
@Builder
class BuildingDescription {
    @JsonProperty("displayNumber")
    private String displayNumber;

    @JsonProperty("receiptDate")
    private String receiptDate;

    @JsonProperty("locationNumber")
    private String locationNumber;

    @JsonProperty("buildingDetails")
    private String buildingDetails;

    @JsonProperty("registrationCause")
    private String registrationCause;
}

@Getter
@Builder
class LandDescription {
    @JsonProperty("displayNumber")
    private String displayNumber;

    @JsonProperty("locationNumber")
    private String locationNumber;

    @JsonProperty("landType")
    private String landType;

    @JsonProperty("area")
    private String area;

    @JsonProperty("registrationCause")
    private String registrationCause;
}

@Getter
@Builder
class ExclusivePartDescription {
    @JsonProperty("buildingPartDescription")
    private List<BuildingPartDescription> buildingPartDescription;

    @JsonProperty("landRightDescription")
    private List<LandRightDescription> landRightDescription;
}

@Getter
@Builder
class BuildingPartDescription {
    @JsonProperty("displayNumber")
    private String displayNumber;

    @JsonProperty("receiptDate")
    private String receiptDate;

    @JsonProperty("partNumber")
    private String partNumber;

    @JsonProperty("buildingDetails")
    private String buildingDetails;

    @JsonProperty("registrationCause")
    private String registrationCause;
}

@Getter
@Builder
class LandRightDescription {
    @JsonProperty("displayNumber")
    private String displayNumber;

    @JsonProperty("landRightType")
    private String landRightType;

    @JsonProperty("landRightRatio")
    private String landRightRatio;

    @JsonProperty("registrationCause")
    private String registrationCause;
}

@Getter
@Builder
class FirstSection {
    @JsonProperty("rankNumber")
    private String rankNumber;

    @JsonProperty("registrationPurpose")
    private String registrationPurpose;

    @JsonProperty("receiptDate")
    private String receiptDate;

    @JsonProperty("registrationCause")
    private String registrationCause;

    @JsonProperty("holderAndAdditionalInfo")
    private String holderAndAdditionalInfo;
}

@Getter
@Builder
class SecondSection {
    @JsonProperty("rankNumber")
    private String rankNumber;

    @JsonProperty("registrationPurpose")
    private String registrationPurpose;

    @JsonProperty("receiptDate")
    private String receiptDate;

    @JsonProperty("registrationCause")
    private String registrationCause;

    @JsonProperty("holderAndAdditionalInfo")
    private String holderAndAdditionalInfo;
}