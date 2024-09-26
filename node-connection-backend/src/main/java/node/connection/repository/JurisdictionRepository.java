package node.connection.repository;

import node.connection.entity.Jurisdiction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JurisdictionRepository extends JpaRepository<Jurisdiction, String> {
}
