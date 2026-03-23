package com.moviecat.aop;

import com.moviecat.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

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
            if (exception.getStatus().is5xxServerError()) {
                log.error("Server exception in method {} after {} ms (status={}): {}",
                        methodName, duration, exception.getStatus().value(), exception.getMessage(), exception);
            } else {
                log.warn("Business exception in method {} after {} ms (status={}): {}",
                        methodName, duration, exception.getStatus().value(), exception.getMessage());
            }
            throw exception;
        } catch (Exception exception) {
            long duration = System.currentTimeMillis() - start;
            log.error("Unexpected error while executing method {} after {} ms: {}",
                    methodName, duration, exception.getMessage(), exception);
            throw exception;
        }
    }
}
