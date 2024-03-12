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

<br/>

### UserService 테스트 코드
- `@ExtendWith(MockitoExtension.class)`: Mock환경에서 테스트를 진행한다.
- `@InjectMocks`: Mock을 주입할 객체를 지정한다.
- `@Mock`: 가짜객체로 생성한다.
- `@Spy`: 만약 가짜가 아닌 진짜 bean을 주입해야할일이 생길경우 사용한다.
- `when`: 스텁이 할일을 지정하고 `thenReturn()`으로 결과물 또한 지정한다.

```java
public class DummyObject {

    protected User newUser(String username, String fullname) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encPassword = passwordEncoder.encode("1234");
        return User.builder()
                .username(username)
                .password(encPassword)
                .email(username + "@nate.com")
                .fullname(fullname)
                .role(UserEnum.CUSTOMER)
                .build();
    }

    protected User newMockUser(Long id, String username, String fullname) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encPassword = passwordEncoder.encode("1234");
        return User.builder()
                .id(id)
                .username(username)
                .password(encPassword)
                .email(username + "@nate.com")
                .fullname(fullname)
                .role(UserEnum.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
```

```java
public class UserServiceTest extends DummyObject {
    // ...
}
```
테스트 클래스에서 상속해서 사용하면 번거롭게 Mock객체나 진짜 객체를 일일이 생성할 필요가 없어진다.

<br/>

### 유효성검사 AOP 적용
```java
@Component
@Aspect
public class CustomValidationAdvice {

    // PointCut
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {}

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMapping() {}

    // Advice
    @Around("postMapping() || putMapping()")
    public Object validationAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof BindingResult) {
                BindingResult bindingResult = (BindingResult) arg;

                if (bindingResult.hasErrors()) {
                    Map<String, String> errorMap = new HashMap<>();

                    for (FieldError error : bindingResult.getFieldErrors()) {
                        errorMap.put(error.getField(), error.getDefaultMessage());
                    }
                    throw new CustomValidationException("유효성검사 실패", errorMap);
                }
            }
        }
        return proceedingJoinPoint.proceed();
    }
}
```
- `@Aspect`: AOP를 적용하기 위해 어노테이션을 붙여준다.
- `@Component`: 빈으로 등록한다.
- `@PointCut("@annotation()")`: 어떤 어노테이션이 붙어있을 경우 실행될지 포인트컷을 지정한다.
- `@Around`, `@Before`, `@After`: 타겟의 전후, 타겟이 실행되기 전, 타겟이 실행된 후로 지정할 수 있다. 위에서 지정한 어노테이션이 실행된 시점을 기준으로 한다.
- `ProceedingJoinPoint`: @Around인 경우에만 조인포인트를 가져올 수 있다.
- `proceedingJoinPoint.getArgs()`: 조인포인트에서 받아온 매개변수를 배열형태로 가져온다. 가져온 배열에 BindingResult가 있을경우에 유효성검사로직을 실행시킬 수 있게 된다.

<br/>

### @BeforeEach로 더미데이터 세팅하기
```java
@BeforeEach
public void setUp() {
    dataSetting();
}

.../

private void dataSetting() {
    userRepository.save(newUser("ssar", "쌀"));
}
```
UserControllerTest에서는 회원가입시 기존에 같은 username을 사용하고 있는 회원이 존재할 경우 예외가 발생되는 것을 검증해야 한다.
따라서 미리 같은 username의 User 데이터를 생성시킬 필요가 있다. 
UserRepository를 `@Autowired`로 주입해오고 `@BeforeEach`를 통해서 각 테스트코드가 실행되기 전마다 User데이터를 생성시켜줄 수 있다.

<br/>

### JWT 토큰을 사용한 인증&인가
JWT토큰을 사용한 **인증** 과정
1. 클라이언트가 `/api/login`경로로 로그인 요청
2. UsernamePasswordAuthenticationFilter의 `attemptAuthentication()`에서 요청내용을 받음
3. 요청내용으로 인증토큰을 생성 및 세션에 저장하고 리턴하면 UserDetailsService의 `LoadUserByUsername()`을 자동으로 수행
4. 로그인이 성공하면 `successfulAuthentication()`을 수행하고 클라이언트에게 결과를 리턴 (리턴과 동시에 세션내용 삭제)
5. 로그인에 실패하면 `unSuccessfulAuthentication()`을 수행하고 클라이언트에게 에러 결과를 리턴

JWT토큰을 사용한 **인가** 과정
1. 클라이언트가 `/api/s/**`경로로 자원 요청
**2. BasicAuthenticationFilter**의 `doFilterInternal()`에서 요청헤더의 JWT 토큰을 검증
3. JWT 토큰 내용을 가지고 `Authentication`을 생성후 강제 로그인 (SecurityContextHolder에 세션 생성)
4. `chain.doFilter()`로 체인 계속 진행
5. 권한이 없을 경우 SecurityConfig에서 정의한 내용대로 예외처리 및 응답 (아직 컨트롤러로 가기 전이기 때문에 ControllerAdvice로 처리 불가)
