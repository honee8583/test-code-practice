package shop.mtcoding.bank.config.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shop.mtcoding.bank.config.auth.LoginUser;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
class JwtAuthorizationFilterTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void authorization_success_test() throws Exception {
        // given
        User user = User.builder().id(1L).role(UserEnum.CUSTOMER).build();
        LoginUser loginUser = new LoginUser(user);
        String token = JwtProcess.create(loginUser);
        System.out.println("테스트: " + token);

        // when
        ResultActions resultActions = mvc.perform(post("/api/s/hello/test").header(JwtVO.HEADER, token));

        // then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void authorization_fail_test() throws Exception {
        // given
        // when
        ResultActions resultActions = mvc.perform(post("/api/s/hello/test"));

        // then
        resultActions.andExpect(status().isUnauthorized()); // 401
    }

    @Test
    void authorization_forbidden_test() throws Exception {
        // given
        User user = User.builder().id(1L).role(UserEnum.CUSTOMER).build();
        LoginUser loginUser = new LoginUser(user);
        String token = JwtProcess.create(loginUser);
        System.out.println("테스트: " + token);

        // when
        ResultActions resultActions = mvc.perform(post("/api/admin/hello/test").header(JwtVO.HEADER, token));

        // then
        resultActions.andExpect(status().isForbidden());
    }
}