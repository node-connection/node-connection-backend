package node.connection.dto.registry.request;

import node.connection.dto.registry.FirstSectionDto;

public record AddFirstSectionEntry(
        String documentId,
        FirstSectionDto firstSection
) {
}
