package node.connection.dto.registry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegistryDocumentDto(
        @JsonProperty("address") String address,
        @JsonProperty("detailAddress") String detailAddress,
        @JsonProperty("id") String id,
        @JsonProperty("titleSection") TitleSectionDto titleSectionDto,
        @JsonProperty("exclusivePartDescription") ExclusivePartDescriptionDto exclusivePartDescriptionDto,
        @JsonProperty("firstSection") List<FirstSectionDto> firstSectionDto,
        @JsonProperty("secondSection") List<SecondSectionDto> secondSectionDto
) {}

