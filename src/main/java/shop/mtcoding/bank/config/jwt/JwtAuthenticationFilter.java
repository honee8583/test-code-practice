package shop.mtcoding.bank.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import shop.mtcoding.bank.config.auth.LoginUser;
import shop.mtcoding.bank.dto.user.UserReqDto.LoginReqDto;
import shop.mtcoding.bank.dto.user.UserRespDto.LoginRespDto;
import shop.mtcoding.bank.util.CustomResponseUtil;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        setFilterProcessesUrl("/api/login");    // 로그인 url 변경
        this.authenticationManager = authenticationManager;
    }

    // POST : /api/login 경로로 요청될경우 동작
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        log.debug("디버그 : attemptAuthentication 호출됨");
        try {
            ObjectMapper om = new ObjectMapper();
            LoginReqDto loginReqDto = om.readValue(request.getInputStream(), LoginReqDto.class);

            // 강제로그인
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginReqDto.getUsername(), loginReqDto.getPassword());

            // UserDetailsService의 LoadUserByUsername()을 호출.
            // JWT를 쓴다 하더라도, 컨트롤러에 진입하면 시큐리티의 권한체크, 인증체크 (authorizeRequest())의 도움을 받을 수 있게 세션을 만든다.
            // 이 세션의 유효기간은 request하고, response하면 끝!! (jSessionId를 사용하지 않기 때문에 다음 요청에서 이 세션을 사용할 수 없다.)
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            return authentication;
        } catch (Exception e) {
            // 꼭 InternalAuthenticationServiceException을 던져야 한다.
            // 필터 내부의 예외는 ControllerAdvice에서 제어할 수 없다.
            // unsuccessfulAuthentication()을 호출한다.
            throw new InternalAuthenticationServiceException(e.getMessage());
        }
    }

    // 로그인실패
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        CustomResponseUtil.fail(response, "로그인 실패", HttpStatus.UNAUTHORIZED);
    }

    // return authentication 잘 작동하면 successfulAuthentication 메서드가 호출된다.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        log.debug("디버그 : successfulAuthentication 호출됨");
        LoginUser loginUser = (LoginUser) authResult.getPrincipal();
        String jwtToken = JwtProcess.create(loginUser);
        response.addHeader(JwtVO.HEADER, jwtToken);

        LoginRespDto loginRespDto = new LoginRespDto(loginUser.getUser());
        CustomResponseUtil.success(response, loginRespDto);
    }
}
