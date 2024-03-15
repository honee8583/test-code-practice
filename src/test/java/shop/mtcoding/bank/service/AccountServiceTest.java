package shop.mtcoding.bank.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.mtcoding.bank.config.dummy.DummyObject;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountListRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest extends DummyObject {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Spy    // 진짜 객체를 injectMocks에 주입한다.
    private ObjectMapper om;

    @Test
    void 계좌등록_test() throws Exception {
        // given
        Long userId = 1L;

        AccountSaveReqDto accountSaveReqDto = new AccountSaveReqDto();
        accountSaveReqDto.setNumber(1111L);
        accountSaveReqDto.setPassword(1234L);

        User ssar = newMockUser(userId, "ssar", "쌀");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(ssar));

        when(accountRepository.findByNumber(anyLong())).thenReturn(Optional.empty());

        Account ssarAccount = newMockAccount(1L, 1111L, 1000L, ssar);
        when(accountRepository.save(any())).thenReturn(ssarAccount);

        // when
        AccountSaveRespDto accountSaveRespDto = accountService.계좌등록(accountSaveReqDto, userId);
        String responseBody = om.writeValueAsString(accountSaveRespDto);
        System.out.println("테스트: " + responseBody);

        // then
        assertThat(accountSaveRespDto.getNumber()).isEqualTo(1111L);
    }

    @Test
    void 계좌목록보기_유저별_test() {
        // given
        Long userId = 1L;

        // stub1
        User user = newMockUser(userId, "ssar", "쌀");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // stub2
        List<Account> accounts = new ArrayList<>();
        accounts.add(newMockAccount(1L, 1111L, 1000L, user));
        accounts.add(newMockAccount(2L, 2222L, 1000L, user));
        when(accountRepository.findByUser_id(anyLong())).thenReturn(accounts);

        // when
        AccountListRespDto accountListRespDto = accountService.계좌목록보기_유저별(userId);

        // then
        assertThat(accountListRespDto.getFullname()).isEqualTo("쌀");
        assertThat(accountListRespDto.getAccounts().size()).isEqualTo(2);
    }

    @Test
    void 계좌삭제_test() throws Exception {
        // given
        Long number = 1111L;
        Long userId = 2L;

        User ssar = newMockUser(1L, "ssar", "쌀");
        Account account = newMockAccount(1L, number, 1000L, ssar);
        when(accountRepository.findByNumber(anyLong())).thenReturn(Optional.of(account));

        // when
        assertThrows(CustomApiException.class, () -> accountService.계좌삭제(number, userId));

        // then
    }
}