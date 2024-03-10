# Junit Bank App

### Jpa LocalDateTime 자동으로 생성하는 법
- @EnableJpaAuditing (Main 클래스)
- @EntityListeners(AuditingEntityListener.class) (Entity 클래스)
```java
@CreatedDate
@Column(nullable = false)
private LocalDateTime createdAt;

@LastModifiedDate
@Column(nullable = false)
private LocalDateTime updatedAt;
```

<br/>

### 스프링 시큐리티 기본 설정
스프링시큐리티는 기본적으로 로그인을 진행하지 않은 상태에서 인증이 필요한 API를 호출할경우 403에러를 반환한다.
하지만 401에러가 더 맞는 상태코드이므로 401에러와 커스텀한 에러메시지를 반환하도록 설정한다. 
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.headers().frameOptions().disable();
    http.csrf().disable();
    http.cors().configurationSource(configurationSource());

    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    http.formLogin().disable();
    http.httpBasic().disable();

    http.exceptionHandling().authenticationEntryPoint((request, response, authException) -> {
        ObjectMapper om = new ObjectMapper();
        ResponseDto<?> responseDto = new ResponseDto<>(-1, "인증안됨", null);
        String responseBody = om.writeValueAsString(responseDto);
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(401);
        response.getWriter().println(responseBody);
    });
    
    http.authorizeRequests()
    .antMatchers("/api/s/**").authenticated()
    .antMatchers("/api/admin/**").hasRole(UserEnum.ADMIN.toString())    // 최근 공식문서에서 "ROLE_" 안붙여도 됨.
    .anyRequest().permitAll();

    return http.build();
}
```
- `SecurityFilterChain`에 설정코드를 입력하고 빈으로 등록한다. (최근 공식문서 기준)
- `http.headers().frameOptions().disable();`: iframe 허용 안함.
- `http.csrf().disable();`: 허용시 포스트맨이 동작 안한다.
- `http.cors().configurationSource(configurationSource());`: Cors 설정.
- `http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);`: jSessionId를 서버쪽에서 관리하지 않는다.
- `http.formLogin().disable();`: 폼로그인 방식을 사용하지 않는다.
- `http.httpBasic().disable();`: 브라우저가 팝업창을 이용해서 사용자 인증을 진행하는것을 막는다.
- `http.exceptionHandling().authenticationEntryPoint()`: 스프링시큐리티에서 예외가 발생하면 ExceptionTranslationFilter가 발생하여 대신 응답함. 따라서 제어권을 가져와서 우리가 대신 응답해야 한다. 여기서는 응답형식은 json, status값으로 401, 그리고 커스텀한 에러메시지를 출력한다. 
- `hasRole(UserEnum.ADMIN.toString())`: 최근 공식문서에서는 "ROLE_"을 붙이 않아도 된다고 함.

<br/>

### 스프링 시큐리티 Cors 설정
```java
public CorsConfigurationSource configurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedHeader("*");
    configuration.addAllowedMethod("*");
    configuration.addAllowedOriginPattern("*");
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
}
```
- 스프링시큐리티 설정에서 Cors설정을 같이 해준다. `CorsConfigurationSource`를 설정해준다.
- `configuration.addAllowedHeader("*")`: 모든 헤더를 허용한다.
- `configuration.addAllowedMethod("*")`: 모든 메소드 유형을 허용한다.
- `configuration.addAllowedOriginPattern("*")`: 모든 IP 주소를 허용한다.
- `configuration.setAllowCredentials(true)`: 클라이언트 측의 쿠키 요청을 허용한다.
- `source.registerCorsConfiguration("/**", configuration)`: 모든 주소에 대해서 설정한 Cors 설정을 반영한다.

<br/>

### SpringConfig 테스트 코드
스프링시큐리티로 특정 API에 대해서 인증을 요구하도록 설정했다. 해당 API들을 인증하지 않은 상태로 요청했을 경우를 테스트한다.
- `@AutoConfigureMockMvc`: Mock환경에 MockMvc를 등록한다.
- `@SpringBootTest(webEnvironment = WebEnvironment.MOCK)`: Mock환경으로 테스트를 진행한다.

<br/>

```java
@Test
void authentication_test() throws Exception {
    // given
    // when
    ResultActions resultActions = mvc.perform(get("/api/s/hello"));
    String responseBody = resultActions.andReturn().getResponse().getContentAsString();
    int httpStatusCode = resultActions.andReturn().getResponse().getStatus();

    // then
    assertThat(httpStatusCode).isEqualTo(401);
}
```
- MockMvc로 보낸 요청에 대한 응답을 `ResultActions` 객체로 받는다. 
- ResultActions객체로부터 응답body의 내용과 HttpStatus코드를 받아와 예상값과 일치하는지 확인한다.  