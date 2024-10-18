package node.connection.repository;

import node.connection.entity.Court;
import node.connection.entity.Jurisdiction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JurisdictionRepository extends JpaRepository<Jurisdiction, String> {

    @Query("select j.court from Jurisdiction j where :address like concat(j.key.city, ' ', j.key.district, '%')")
    Optional<Court> findCourtByAddress(String address);
}
