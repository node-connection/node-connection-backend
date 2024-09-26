package node.connection.dto.registry.request;

import node.connection.dto.registry.SecondSectionDto;

public record AddSecondSectionEntry(
        String documentId,
        SecondSectionDto secondSection
) {
}
