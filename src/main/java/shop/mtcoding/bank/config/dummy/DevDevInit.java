package shop.mtcoding.bank.config.dummy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;

@Configuration
public class DevDevInit extends DummyObject {

    @Profile("dev") // prod 모드에서는 실행되면 안된다.
    @Bean
    CommandLineRunner init(UserRepository userRepository, AccountRepository accountRepository) {
        return (args) -> {
            // 서버 실행시에 무조건 실행된다.
            User ssar = userRepository.save(newUser("ssar", "쌀"));
            User cos = userRepository.save(newUser("cos", "코스"));
            Account ssarAccount = accountRepository.save(newMockAccount(1L, 1111L, 1000L, ssar));
            Account cosAccount = accountRepository.save(newMockAccount(2L, 2222L, 1000L, cos));
        };
    }
}
