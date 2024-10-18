package node.connection.repository;

import node.connection.entity.IssuanceHistory;
import node.connection.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssuanceHistoryRepository extends JpaRepository<IssuanceHistory, UserAccount> {
    List<IssuanceHistory> findAllByUserAccount(UserAccount userAccount);
}
