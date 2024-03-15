package shop.mtcoding.bank.domain.account;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // @Query("SELECT ac FROM Account ac JOIN FETCH ac.user u WHERE ac.number = :number")
    // join fetch를 하면 조인해서 객체의 값을 미리 가져올 수 있다.
    // 그렇다면 EntityGraph는 미리 가져올 필드를 지정할 싶을 경우 사용.
    Optional<Account> findByNumber(Long number);

    List<Account> findByUser_id(Long userId);
}
