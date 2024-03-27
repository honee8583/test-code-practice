package shop.mtcoding.bank.domain.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import shop.mtcoding.bank.config.dummy.DummyObject;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;

@ActiveProfiles("test")
@DataJpaTest    // DB 관련 Bean이 다 올라온다. 기본적으로 @Transactional이 적용되어 있다.
public class TransactionRepositoryImplTest extends DummyObject {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp() {
        autoincrementReset();
        dataSetting();
        em.clear();
    }

    @Test
    void findTransactionList_all_test() {
        // given
        Long accountId = 1L;

        // when
        List<Transaction> transactionListPS = transactionRepository.findTransactionList(accountId, "ALL", 0);
        for (Transaction t : transactionListPS) {
            System.out.println("테스트: id " + t.getId());
            System.out.println("테스트: amount " + t.getAmount());
            System.out.println("테스트: sender " + t.getSender());
            System.out.println("테스트: receiver " + t.getReceiver());
            System.out.println("테스트: withdrawAccount 잔액 " + t.getWithdrawAccountBalance());
            System.out.println("테스트: depositAccount 잔액 " + t.getDepositAccountBalance());
            System.out.println("테스트: withdrawAccount balance " + t.getWithdrawAccount().getBalance());
//            System.out.println("테스트: fullname " + t.getWithdrawAccount().getUser().getFullname());
            System.out.println("============================");
        }

        // then
        assertThat(transactionListPS.get(3).getDepositAccountBalance()).isEqualTo(800L);
    }

    @Test
    void dataJpa_test1() {
        List<Transaction> transactionList = transactionRepository.findAll();
        for (Transaction transaction : transactionList) {
            System.out.println("테스트: " + transaction.getId());
            System.out.println("테스트: " + transaction.getSender());
            System.out.println("테스트: " + transaction.getReceiver());
            System.out.println("테스트: " + transaction.getGubun());
            System.out.println("============================");
        }
    }

    @Test
    void dataJpa_test2() {
        List<Transaction> transactionList = transactionRepository.findAll();
        for (Transaction transaction : transactionList) {
            System.out.println("테스트: " + transaction.getId());
            System.out.println("테스트: " + transaction.getSender());
            System.out.println("테스트: " + transaction.getReceiver());
            System.out.println("테스트: " + transaction.getGubun());
            System.out.println("============================");
        }
    }

    private void dataSetting() {
        User ssar = userRepository.save(newUser("ssar", "쌀"));
        User cos = userRepository.save(newUser("cos", "코스"));
        User love = userRepository.save(newUser("love", "러브"));
        User admin = userRepository.save(newUser("admin", "관리자"));

        Account ssarAccount1 = accountRepository.save(newAccount(1111L, ssar));
        Account cosAccount = accountRepository.save(newAccount(2222L, cos));
        Account loveAccount = accountRepository.save(newAccount(3333L, love));
        Account ssarAccount2 = accountRepository.save(newAccount(4444L, ssar));

        Transaction withdrawTransaction1 = transactionRepository
                .save(newWithdrawTransaction(ssarAccount1, accountRepository));
        Transaction depositTransaction1 = transactionRepository
                .save(newDepositTransaction(cosAccount, accountRepository));

        Transaction transferTransaction1 = transactionRepository
                .save(newTransferTransaction(ssarAccount1, cosAccount, accountRepository));
        Transaction transferTransaction2  = transactionRepository
                .save(newTransferTransaction(ssarAccount1, loveAccount, accountRepository));
        Transaction transferTransaction3 = transactionRepository
                .save(newTransferTransaction(cosAccount, ssarAccount1, accountRepository));
    }

    private void autoincrementReset() {
        em.createNativeQuery("ALTER TABLE user_tb ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE account_tb ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE transaction_tb ALTER COLUMN id RESTART WITH 1").executeUpdate();
    }
}
