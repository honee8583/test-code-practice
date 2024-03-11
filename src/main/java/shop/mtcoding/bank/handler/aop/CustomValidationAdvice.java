package shop.mtcoding.bank.handler.aop;

import java.util.HashMap;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import shop.mtcoding.bank.handler.ex.CustomValidationException;

@Component
@Aspect
public class CustomValidationAdvice {

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {}

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMapping() {}

    @Around("postMapping() || putMapping()")    // JoinPoint의 전후 제어
    public Object validationAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {   // Around만 JoinPoint를 받아올 수 있다.
        Object[] args = proceedingJoinPoint.getArgs();  // JoinPoint의 매개변수
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
        return proceedingJoinPoint.proceed();   // 다시 정상적으로 해당 메서드를 실행
    }
}
