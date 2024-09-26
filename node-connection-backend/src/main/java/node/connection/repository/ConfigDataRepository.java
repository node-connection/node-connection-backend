package node.connection.repository;
import node.connection.entity.ConfigData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfigDataRepository extends JpaRepository<ConfigData, Long> {
    Optional<ConfigData> findByKey(String key);
}