package node.connection.dto.registry;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RegistryDocumentDto(
        @JsonProperty("id") String id,
        @JsonProperty("titleSection") TitleSectionDto titleSectionDto,
        @JsonProperty("exclusivePartDescription") ExclusivePartDescriptionDto exclusivePartDescriptionDto,
        @JsonProperty("firstSection") List<FirstSectionDto> firstSectionDto,
        @JsonProperty("secondSection") List<SecondSectionDto> secondSectionDto
) {}

