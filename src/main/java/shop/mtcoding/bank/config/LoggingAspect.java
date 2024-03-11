package shop.mtcoding.bank.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect  // 해당클래스가 aspect임을 명시
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // aspect가 적용될 포인트컷을 지정
    // 지정한 패키지내의 모든 메소드를 포인트 컷으로 지정
    @Pointcut("within(shop.mtcoding.bank..*)")
    public void loggingPointcut() {}

    // joinPoint가 실행되기 이전에 실행될 어드바이스를 정의
    @Before("loggingPointcut()")
    public void logBefore(JoinPoint joinPoint) {
        log.debug("Enter: {} with argument[s] = {}", joinPoint.getSignature().getName(), joinPoint.getArgs());
    }

    // joinPoint에서 성공적으로 반환된 후에 실행될 어드바이스를 정의
    @AfterReturning(pointcut = "loggingPointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.debug("Exit: {} with result = {}", joinPoint.getSignature().getName(), result);
    }

    // joinPoint에서 예외가 발생된 후에 실행될 어드바이스를 정의
    @AfterThrowing(pointcut = "loggingPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error("Exception in {}.{}() with cause = {}", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(), e.getCause() != null ? e.getCause() : "NULL");
    }
}