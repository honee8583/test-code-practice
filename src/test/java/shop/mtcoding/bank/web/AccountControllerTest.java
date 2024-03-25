package shop.mtcoding.bank.web;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shop.mtcoding.bank.config.dummy.DummyObject;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountDepositReqDto;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountWithdrawReqDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@Sql("classpath:db/teardown.sql")
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
class AccountControllerTest extends DummyObject {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    public void setUp() {
        User ssar = userRepository.save(newUser("ssar", "쌀"));
        User cos = userRepository.save(newUser("cos", "코스"));
        Account ssarAccount = accountRepository.save(newMockAccount(1L, 1111L, 1000L, ssar));
        Account cosAccount = accountRepository.save(newMockAccount(2L, 2222L, 1000L, cos));

        em.clear(); // 실제로는 PC에 내용이 없어야하기 때문에 환경을 맞춰줘야 한다.
    }

    // 세션값만 만들어주면 된다.
    // setupBefore=TEST_METHOD (setUp 메서드 실행전에 수행)
    // setupBefore=TEST_EXECUTION (saveAccount_test() 실행전에 수행)
    @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION)    // DB에서 username=ssar 조회를 해서 세션에 담아주는 어노테이션. 즉 DB에 존재하는 유저여야 한다.
    @Test
    void saveAccount_test() throws Exception {
        // given
        AccountSaveReqDto accountSaveReqDto = new AccountSaveReqDto();
        accountSaveReqDto.setNumber(9999L);
        accountSaveReqDto.setPassword(1234L);
        String requestBody = om.writeValueAsString(accountSaveReqDto);
        System.out.println("테스트: " + requestBody);

        // when
        ResultActions resultActions = mvc.perform(post("/api/s/account")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트: " + responseBody);

        // then
        resultActions.andExpect(status().isCreated());
    }

    @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void findUserAccount_test() throws Exception {
        // given
        // when
        ResultActions resultActions = mvc.perform(get("/api/s/account/login-user"));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.fullname").value("쌀"));
        resultActions.andExpect(jsonPath("$.data.accounts.size()").value(1));
    }

    /*
    * 테스트시에는 insert한것들이 PC에 올라간다.
    * 영속화된것들을 초기화해주는 것이 개발 모드와 동일한 환경으로 테스트할 수 있다.
    * 최초의 select는 쿼리가 발생하지만 PC에 이미 있을 경우 1차캐시를 함.
    * 이미 있을 경우 Lazy로딩은 쿼리도 발생하지 않는다.
    * Lazy로딩할 때 PC에 없다면 쿼리는 발생한다.
    */
    @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void deleteAccount_test() throws Exception {
        // given
        Long number = 1111L;

        // when
        ResultActions resultActions = mvc.perform(delete("/api/s/account/" + number));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트: " + responseBody);

        // then
        // JUnit테스트에서 delete쿼리는 DB관련으로 가장 마지막에 실행되면 발동 안됨.
        assertThrows(CustomApiException.class, () -> accountRepository.findByNumber(number).orElseThrow(() -> new CustomApiException("계좌를 찾을 수 없습니다")));
    }

    @Test
    void deposit_test() throws Exception {
        // given
        AccountDepositReqDto request = new AccountDepositReqDto();
        request.setNumber(1111L);
        request.setAmount(100L);
        request.setGubun("DEPOSIT");
        request.setTel("01011112222");

        String requestBody = om.writeValueAsString(request);
        System.out.println("테스트: " + requestBody);

        // when
        ResultActions resultActions = mvc.perform(post("/api/account/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트: " + responseBody);

        // then
        resultActions.andExpect(status().isOk());
    }

    @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void withdrawACcount_test() throws Exception {
        // given
        AccountWithdrawReqDto accountWithdrawReqDto = new AccountWithdrawReqDto();
        accountWithdrawReqDto.setNumber(1111L);
        accountWithdrawReqDto.setPassword(1234L);
        accountWithdrawReqDto.setAmount(100L);
        accountWithdrawReqDto.setGubun("WITHDRAW");

        String requestBody = om.writeValueAsString(accountWithdrawReqDto);
        System.out.println("테스트: " + requestBody);

        // when
        // then
        ResultActions resultActions = mvc.perform(get("/api/s/account/withdraw")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트: " + responseBody);
    }
}