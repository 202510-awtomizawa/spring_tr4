package com.example.todo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {

  private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);

  @Pointcut("execution(* com.example.todo.service..*(..))")
  public void serviceMethods() {
    // Pointcut for all service methods
  }

  @Around("serviceMethods()")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.nanoTime();
    try {
      return joinPoint.proceed();
    } finally {
      long end = System.nanoTime();
      long elapsedMs = (end - start) / 1_000_000;
      String methodName = joinPoint.getSignature().toShortString();
      logger.info("Performance: method={} timeMs={}", methodName, elapsedMs);
    }
  }
}
