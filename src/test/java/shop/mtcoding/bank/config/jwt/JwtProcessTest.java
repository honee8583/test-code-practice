package shop.mtcoding.bank.config.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import shop.mtcoding.bank.config.auth.LoginUser;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;

class JwtProcessTest {

    @Test
    void create_test() {
        // given
        User user = User.builder().id(1L).role(UserEnum.CUSTOMER).build();  // JWT를 생성할 때 필요한 정보로만 구성
        LoginUser loginUser = new LoginUser(user);

        // when
        String jwtToken = JwtProcess.create(loginUser);
        System.out.println("테스트: " + jwtToken);

        // then
        assertTrue(jwtToken.startsWith(JwtVO.TOKEN_PREFIX));
    }

    @Test
    void verify_test() {
        // given
        User user = User.builder().id(1L).role(UserEnum.CUSTOMER).build();  // JWT를 생성할 때 필요한 정보로만 구성
        LoginUser loginUser = new LoginUser(user);
        String jwtToken = JwtProcess.create(loginUser).replace(JwtVO.TOKEN_PREFIX, "");
        System.out.println("테스트: " + jwtToken);

        // when
        LoginUser verifiedLoginUser = JwtProcess.verify(jwtToken);
        System.out.println("테스트: " + loginUser.getUser().getId());

        // then
        assertThat(verifiedLoginUser.getUser().getId()).isEqualTo(1L);
        assertThat(verifiedLoginUser.getUser().getRole()).isEqualTo(UserEnum.CUSTOMER);
    }
}