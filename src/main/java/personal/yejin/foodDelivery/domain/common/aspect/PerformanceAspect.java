package personal.yejin.foodDelivery.domain.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);

    @Around("execution(* personal.yejin.foodDelivery.domain.rider.service.RiderService.assignRider(..))")
    public Object measureMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();

        Object result = joinPoint.proceed(); // 대상 메서드 실행

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 밀리초 단위

        logger.info("{} 메서드 실행 시간: {} ms", joinPoint.getSignature().toShortString(), duration);

        return result;
    }
}
