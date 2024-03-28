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
스프링시큐리티는 기본적으로 로그인을 진행하지 않은 상태에서 인증이 필요한 API를 호출할경우 403에러(권한없음)를 반환한다.
하지만 401에러(인증안됨)가 더 맞는 상태코드이므로 401에러와 커스텀한 에러메시지를 반환하도록 설정한다. 
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

### Mock환경에 진짜 객체 가져오기
```java
@Spy
private ObjectMapper om;
```
@Spy 어노테이션을 사용해서 객체를 주입하면 Mock환경에 진짜 객체를 DI해오기 때문에 해당 객체의 기능을 사용할 수 있다.

<br/>

### @BeforeEach로 더미데이터 세팅하기
```java
@Transactional
public class UserServiceTest {
    @BeforeEach
    public void setUp() {
        dataSetting();
    }
    
    // ...
    
    private void dataSetting() {
        userRepository.save(newUser("ssar", "쌀"));
    }
}
```
UserControllerTest에서는 회원가입시 기존에 같은 username을 사용하고 있는 회원이 존재할 경우 예외가 발생되는 것을 검증해야 한다.  
따라서 미리 같은 username의 User 데이터를 생성시킬 필요가 있다.  
UserRepository를 `@Autowired`로 주입해오고 `@BeforeEach`를 통해서 각 테스트코드가 실행되기 전마다 User데이터를 생성시켜줄 수 있다.  
단, 각 테스트 코드마다 실행되게 되면 같은 id값으로 데이터를 생성하게 될경우 예외가 발생하기 때문에 클래스 단위에 `@Transactional`을 붙여주면 테스트코드가 완료될때마다 데이터를 롤백시켜 중복을 방지해줄 수 있다.

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
2. **BasicAuthenticationFilter**의 `doFilterInternal()`에서 요청헤더의 JWT 토큰을 검증
3. JWT 토큰 내용을 가지고 `Authentication`을 생성후 강제 로그인 (SecurityContextHolder에 세션 생성)
4. JWT토큰이 헤더로 요청이 들어오든 안들어오든 `chain.doFilter()`로 체인 계속 진행
5. 권한이 없을 경우 SecurityConfig에서 정의한 내용대로 예외처리 및 응답 (아직 컨트롤러로 가기 전이기 때문에 ControllerAdvice로 처리 불가)
6. 토큰이 들어오지 않았을 경우 컨트롤러로 요청이 갈때 SecurityConfig에서 '인증안됨'에 해당되는 예외처리를 진행

<br/>

### Security 필터 등록
```java
public class CustomSecurityFilterManager extends AbstractHttpConfigurer<CustomSecurityFilterManager, HttpSecurity> {
    @Override
    public void configure(HttpSecurity builder) throws Exception {
        AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
        builder.addFilter(new JwtAuthenticationFilter(authenticationManager)); // 필터 추가
        builder.addFilter(new JwtAuthorizationFilter(authenticationManager));  // 필터 추가
        super.configure(builder);
    }
}

// ...
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // 생략..
    http.apply(new CustomSecurityFilterManager());
}
```
앞서 생성한 JWT 인증, 인가 필터를 Security에 등록하기 위해서는 `AbstractHttpConfigurer<>`를 상속한 클래스를 생성해야 한다.  
해당 클래스에 각 필터에서 필요한 AuthenticationManager를 받아오고 필터를 생성 후 HttpSecurity객체에 필터를 추가해준다.  
해당 클래스는 `filterChain()`에서 `apply()` 메소드를 사용해서 다시 등록해준다.

<br/>

### 인증이 필요한 테스트 코드 @WithUserDetails
```java
@BeforeEach
public void setUp() {
User user = userRepository.save(newUser("ssar", "쌀"));
}

// ...

@WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION)
@Test
void saveAccount_test() throws Exception {
    // ...
}
```
인증이 필요한 url요청을 테스트 코드에서 해야 하는 경우 JWT 토큰이 필요로 한다고 생각할 수 있지만 구체적으로는 인증정보가 담긴 세션만 있으면 된다.  
따라서 `@WithUserDetails` 어노테이션으로 value옵션으로 지정한 username으로 세션정보를 만들어낼 수 있다.  
하지만 이 방식을 사용하려면 DB에 이에 해당하는 User정보가 이미 존재해야 한다. 이를 위해 `@BeforeEach`를 통해서 DB에 데이터를 집어넣는다 한들 예외가 발생한다.  
setupBefore옵션을 지정해주지 않을 경우 기본적으로 `TEST_METHOD` 방식으로 실행되는데 이는 `@BeforeEach` 메소드가 실행되기 전에 세션을 만들려고 하므로 다시 DB에 데이터가 존재하지 않아 발생한다.  
따라서 setupBefore옵션을 `TEST_EXECUTION`으로 지정해주면 해당 테스트 메소드가 실행되기 전에 실행되므로 무사히 세션정보를 생성해내고 요청을 성공할 수 있다.

<br/>

### 테스트 코드 작성시 PersistenceContext 관리
삭제로직을 구현하고 직접 포스트맨이나 http요청으로 테스트할 때는 삭제할 데이터가 이미 DB에 존재하기 때문에 문제가 존재하지 않는다.
하지만 테스트할 때는 `@BeforeEach`로 테스트 로직이 동작하기 전에 데이터를 넣게 된다. 따라서 insert한 엔티티는 아직 PersistenceContext에 존재하는 상태이다.
이러한 이유로 테스트 코드가 동작할 때는 Lazy 로딩으로 연관된 엔티티에 대한 정보를 불러올 때 PersistenceContext에서 엔티티를 꺼내 조회하기 때문에 쿼리문이 찍히지 않는다.
개발할때의 테스트와 테스트 코드의 환경을 동일하게 맞춰주기 위해서는 PersistenceContext의 상태도 동일하게 맞춰줘야 한다. 따라서 `@BeforeEach`에서 데이터를 insert한 후에는 
**EntityManager**를 사용해서 clear해 비워준다.
```java
@BeforeEach
public void setUp() {
    // 생략...

    em.clear(); // 실제로는 PC에 내용이 없어야하기 때문에 환경을 맞춰줘야 한다.
}
```

<br/>

### @Transactional 대신 @Sql로 teardown 구문 실행하기
`@Transactional`을 설정해서 각 테스트코드가 실행된 후 롤백을 시켜줄 수 있었지만, id값은 초기화되지 않는다. 
즉 첫번째 테스트코드에서 `@BeforeEach`로 하나의 엔티티를 저장하면 id값은 1로 저장되고 
두번째 테스트코드에서 데이터베이스의 내용이 롤백되고 `@BeforeEach`로 똑같이 하나의 엔티티를 저장하면 우리가 원한 id값 1이 아닌
2가 저장된다.  
<br/>

이로인해 발생할 수 있는 문제점은 여러개의 테스트코드를 작성하게 되면 각 테스트 코드마다 똑같이 저장하게 되는 데이터의 id값도 점점 
증가해 나중에는 얼마나 늘어난 id값을 일일이 기억해 코드를 짜야 한다. 즉 독립적이지 않은 테스트 코드를 작성해야 한다.  
<br/>
이를 해결하기 위한 sql문을 작성해 저장하고 테스트 클래스에서 `@Sql` 어노테이션으로 불러와서 `@BeforeEach`가 실행되기 전에 
실행시키면 된다.  
<br/>
resources 폴더내에 db폴더를 생성하고 teardown.sql파일을 생성해 아래 코드를 작성하자.  
<br/>

```sql
-- resources 폴더
SET REFERENTIAL_INTEGRITY FALSE;
truncate table transaction_tb;
truncate table account_tb;
truncate table user_tb;
SET REFERENTIAL_INTEGRITY TRUE;
```
- `SET REFERENTIAL_INTEGRITY FALSE`: 걸려있는 제약 조건을 비활성화한다.
- `truncate table {테이블명}`: 테이블을 DROP하는 것이 아닌 내용을 지워줌으로써 DROP후 다시 CREATE문을 실행해야하는 단점을 보완할 수 있다.
- `SET REFERENTIAL_INTEGRITY TRUE`: 걸려있는 제약 조건을 다시 활성화한다.

<br/>

```java
@Sql("classpath:db/teardown.sql")
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
class AccountControllerTest {
    // ...
}
```
`@Sql` 어노테이션을 사용해 classpath 즉 resources의 db폴더의 작성한 `teardown.sql`을 가져와서 `@BeforeEach`가 실행되기 전마다 실행
시켜줌으로써 id필드를 초기화시켜줄 수 있다.
컨트롤러 테스트와 같이 `@SpringBootTest`를 사용하는 곳(통합테스트)에서는 이를 적용시켜주는 것이 좋다.

<br/>

### 특정컬럼을 클라이언트에게 전달하고 싶지 않을 경우
ATM으로 계좌입금을 하면 상대의 잔액이 보여져서는 안된다. 하지만 개발자 입장에서는 잔액을 비교해 테스트하는 것이 편하다.  
이렇게 테스트 용도로만 사용하고 클라이언트에게 전달하고 싶지 않은 필드값이 존재하면 @JsonIgnore 어노테이션을 통해 쉽게 처리할 수 있다.
```java
@JsonIgnore
private Long balance;
```

<br/>

### 스텁마다 연관된 객체는 새로 만들어주자
계좌입금 서비스 로직은 계좌를 찾고 해당 계좌의 잔액을 입금요청한 금액에 맞게 업데이트를 한 후 해당 계좌를 사용해 거래 히스토리를 생성한 후 저장한다.
그렇다면 이 로직의 스텁은 2개가 된다. 하나는 계좌를 찾는 스텁, 또 하나는 트랜잭션을 저장하는 스텁이다. 
만약 테스트 코드에서 하나의 계좌를 생성한후 각 스텁에서 사용하게 된다면 해당 계좌에서 잔액 업데이트 로직이 2번 실행되게 된다.
그 이유는 테스트 코드에서 트랜잭션을 생성할 때 한번, 실제 서비스 메서드가 실행될 때 한번 수행되기 때문이다. 
따라서 각 스텁마다 계좌를 새롭게 생성해 첫번째 스텁의 계좌는 실제 서비스 메서드에서 1번, 두번째 스텁의 계좌는 트랜잭션이 생성될 때 한번 수행하게돼
각 계좌마다 입금이 1번씩 수행된다. 그러므로 스텁마다 연관된 객체는 새로 만들어준다. 

<br/>

### 스프링 시큐리티 Cors 설정 테스트
html파일(fetch-test.html로 작성)을 생성하고 폼태그를 사용하여 사용자의 아이디와 패스워드를 입력받고 로그인 요청을 하는 화면을 
만든다. 서버의 현재 cors 설정 상태로 요청을 하게 되면 응답값의 헤더의 'Authorization'의 값을 출력해봐도 null이 뜬다.
SecurityConfig에서 exposedHeaders로 'Authorization'을 지정해주지 않는다면 브라우저는 해당 헤더값을 노출시키지 않아 
자바스크립트로 조작이 불가능해진다. 따라서 해당 설정을 추가해줘야 한다. 

```java
public CorsConfigurationSource configurationSource() {
        // 생략...

        configuration.addExposedHeader("Authorization");

        // 생략...
    }
```
해당 설정을 해주지 않을 경우 프스트맨과 같은 경우에는 헤더값을 확인할 수 있지만, 브라우저에서는 노출을 시켜주지 않기 때문에 
expose를 해줘야 브라우저에서 헤더값을 JS로 당겨올 수 있다.(이전에는 default값이었지만 현재는 아니므로 설정해줘야 한다.)

<br/>

**인프런 메타코딩의 '스프링부트 JUnit 테스트 - 시큐리티를 활용한 Bank 애플리케이션'을 참고했습니다. 