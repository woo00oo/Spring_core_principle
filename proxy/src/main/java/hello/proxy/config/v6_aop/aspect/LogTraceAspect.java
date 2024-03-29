package hello.proxy.config.v6_aop.aspect;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.lang.reflect.Method;

@Slf4j
@Aspect
public class LogTraceAspect {

    private final LogTrace logTrace;

    public LogTraceAspect(LogTrace logTrace) {
        this.logTrace = logTrace;
    }

    @Around("execution(* hello.proxy.app..*(..)) && !execution(* hello.proxy.app..noLog(..))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        TraceStatus status = null;
        try {
            String message = joinPoint.getSignature().toShortString();
            status = logTrace.begin(message);

            //로직 호출
            Object result = joinPoint.proceed();

            logTrace.end(status);
            return result;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}

/**
 * 자동 프록시 생성기는 2가지 일을 한다.
 * 1. @Aspect를 보고 어드바이저(Advisor)로 변환해서 저장한다.
 * 2. 어드바이저를 기반으로 프록시를 생성한다.
 *
 * 1. @Aspect를 어드바이저로 변환해서 저장하는 과정
 *
 *   1) 실행 : 스프링 애플리케이션 로딩 시점에 자동 프록시 생성기를 호출한다.
 *   2) 모든 @Aspect 빈 조회 : 자동 프록시 생성기는 스프링 컨테이너에서 @Aspect 애노테이션이 붙은 스프링 빈을 모두 조회한다.
 *   3) 어드바이저 생성 : @Aspect 어드바이저 빌더를 통해 @Aspect 애노테이션 정보를 기반으로 어드바이저를 생성한다.
 *   4) @Aspect 기반 어드바이저 저장 : 생성한 어드바이저를 @Aspect 어드바이저 빌더 내부에 저장한다.
 *
 * 2. 어드바이저를 기반으로 프록시 생성
 *
 *   1) 생성 : 스프링 빈 대상이 되는 객체를 생성한다. (@Bean, 컴포넌트 스캔 모두 포함)
 *   2) 전달 : 생성된 객체를 빈 저장소에 등록하기 직전에 빈 후처리기에 전달한다.
 *   3-1) Advisor 빈 조회 : 스프링 컨테이너에서 Advisor 빈을 모두 조회한다.
 *   3-2) @Aspect Advisor 조회 : @Aspect 어드바이저 빌더 내부에 저장된 Advisor를 모두 조회한다.
 *   4) 프록시 적용 대상 체크 : 앞서 3-1, 3-2에서 조회한 Advisor에 포함되어 있는 포인트컷을 사용해서 해당 객체가 프록시를 적용할 대상인지 아닌지 판단한다.
 *     이때 객체의 클래스 정보는 물론이고, 해당 객체의 모든 메서드를 포인트컷에 하나하나 모두 매칭해본다. 그래서 조건이 하나라도 만족하면 프록시 적용 대상이 된다.
 *     예를 들어서 메서드 하나만 포인트컷 조건에 만족해도 프록시 적용 대상이 된다.
 *   5) 프록시 생성 : 프록시 적용 대상이 되면 프록시를 생성하고 프록시를 반환한다. 그래서 프록시를 스프링 빈으로 등록한다.
 *     만약 프록시 적용 대상이 아니라면 원본 객체를 반환해서 원본 객체를 스프링 빈으로 등록한다.
 *   6) 빈 등록 : 반환된 객체는 스프링 빈으로 등록된다.
 */