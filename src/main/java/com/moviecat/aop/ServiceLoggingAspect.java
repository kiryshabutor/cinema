package com.moviecat.aop;

import com.moviecat.exception.ApiException;
import com.moviecat.exception.LoggingException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    private static final String ERROR_EXECUTING_METHOD = "Error executing method!";

    @Around("execution(* com.moviecat.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("Method {} executed in {} ms", methodName, duration);
            return result;
        } catch (ApiException exception) {
            long duration = System.currentTimeMillis() - start;
            log.warn("Business exception in method {} after {} ms: {}", methodName, duration, exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            long duration = System.currentTimeMillis() - start;
            log.error("Unexpected error while executing method {} after {} ms: {}",
                    methodName, duration, exception.getMessage(), exception);
            throw new LoggingException(ERROR_EXECUTING_METHOD, exception);
        }
    }
}
