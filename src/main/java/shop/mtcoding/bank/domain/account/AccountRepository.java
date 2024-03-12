package shop.mtcoding.bank.domain.account;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // TODO 리팩토링 (계좌 소유자 확인시에 쿼리가 2번 나가기때문에 join fetch)
    Optional<Account> findByNumber(Long number);
}
