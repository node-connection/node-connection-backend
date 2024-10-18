package node.connection.repository;

import node.connection.entity.Court;
import node.connection.entity.pk.CourtKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourtRepository extends JpaRepository<Court, CourtKey> {
    Optional<Court> findByRegisterCode(String registerCode);
}
