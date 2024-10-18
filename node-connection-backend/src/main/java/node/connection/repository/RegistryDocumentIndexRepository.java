package node.connection.repository;

import node.connection.entity.RegistryDocumentIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistryDocumentIndexRepository extends JpaRepository<RegistryDocumentIndex, String> {
}
