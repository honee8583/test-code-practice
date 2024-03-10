package shop.mtcoding.bank.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import shop.mtcoding.bank.domain.user.UserEnum;

@Configuration
public class SecurityConfig {
    // @Slf4j를 사용할경우 Junit에서 문제 발생.
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.debug("디버그: BCryptPasswordEncoder 빈 등록됨");
        return new BCryptPasswordEncoder();
    }

    // JWT 필터 등록 필요

    // JWT -> Session 사용x
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers().frameOptions().disable();    // iframe 허용안함
        http.csrf().disable();  // 허용시 포스트맨 작동안함.
        http.cors().configurationSource(configurationSource());

        // jSessionId를 서버쪽에서 관리x.
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.formLogin().disable();
        // httpBasic은 브라우저가 팝업창을 이용해서 사용자인증을 진행.
        http.httpBasic().disable();

        http.authorizeRequests()
                .antMatchers("/api/s/**").authenticated()
                .antMatchers("/api/admin/**").hasRole(UserEnum.ADMIN.toString())    // 최근 공식문서에서 "ROLE_" 안붙여도 됨.
                .anyRequest().permitAll();

        return http.build();
    }

    public CorsConfigurationSource configurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");    // 모든 헤더 허용
        configuration.addAllowedMethod("*");    // GET, POST, PUT, DELETE
        configuration.addAllowedOriginPattern("*"); // 모든 IP 주소 혀용 (프론트엔드 IP만 허용)
        configuration.setAllowCredentials(true);    // 클라이언트 쪽에서 쿠키요청 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 주소에서 위의 설정 적용

        return source;
    }
}
