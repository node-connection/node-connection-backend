package node.connection.data.registry;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RegistryDocument(
        @JsonProperty("id") String id,
        @JsonProperty("titleSection") List<TitleSection> titleSection,
        @JsonProperty("exclusivePartDescription") List<ExclusivePartDescription> exclusivePartDescription,
        @JsonProperty("firstSection") List<FirstSection> firstSection,
        @JsonProperty("secondSection") List<SecondSection> secondSection
) {}

