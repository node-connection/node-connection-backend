package node.connection.repository;

import node.connection.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    Optional<UserAccount> findByFabricId(String fabricId);

    boolean existsByFabricId(String fabricId);

    Optional<UserAccount> findByNumber(String number);
}
