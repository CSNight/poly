package csnight.spider.poly.aop;


import com.alibaba.fastjson.JSONObject;
import csnight.spider.poly.utils.RespTemplate;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.nio.file.AccessDeniedException;

@Component
@Aspect
@Order(2)
public class LogAspectHttp {
    private Logger logger = LoggerFactory.getLogger(LogAspectHttp.class);

    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut(value = "@annotation(csnight.spider.poly.aop.LogAsync)")
    public void aop_cut() {
    }

    @Around(value = "aop_cut()", argNames = "process")
    public Object logHandler(ProceedingJoinPoint process) {
        MethodSignature methodSignature = (MethodSignature) process.getSignature();
        Method method = methodSignature.getMethod();
        String methodName = method.getName();
        Object[] args = process.getArgs();
        StringBuilder params = new StringBuilder();
        for (Object arg : args) {
            if (arg != null) {
                params.append(arg);
                params.append(";");
            }
        }
        startTime.set(System.currentTimeMillis());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest req = attributes.getRequest();
        HttpServletResponse rep = attributes.getResponse();
        Object result;
        long costTime;
        try {
            result = process.proceed();
        } catch (Throwable throwable) {
            costTime = System.currentTimeMillis() - startTime.get();
            assert rep != null;
            logger.error("{} {} {} {} {} Params:{}\r\nCost:{}ms\r\nError => {}", req.getRemoteHost(), req.getMethod(),
                    rep.getStatus(), req.getRequestURI(), methodName, params, costTime, throwable.getMessage());
            if (throwable instanceof AccessDeniedException) {
                result = new RespTemplate(403, HttpStatus.FORBIDDEN, throwable.getMessage(), req.getRequestURI(), req.getMethod());
            } else {
                result = new RespTemplate(500, HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage(), req.getRequestURI(), req.getMethod());
            }
        }
        costTime = System.currentTimeMillis() - startTime.get();
        assert rep != null;
        Object wrapRes = "";
        if (result instanceof RespTemplate) {
            wrapRes = JSONObject.toJSONString(result);
        } else {
            wrapRes = result;
        }
        logger.info("{} {} {} {} {} Params:{}\r\nCost:{}ms\r\nResponse => {}", req.getRemoteHost(), req.getMethod(),
                rep.getStatus(), req.getRequestURI(), methodName, params, costTime, wrapRes);
        return result;
    }

    /**
     * 配置异常通知
     *
     * @param joinPoint join point for advice
     * @param e         exception
     */
    @AfterThrowing(pointcut = "aop_cut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        logger.error(e.getMessage());
    }
}
