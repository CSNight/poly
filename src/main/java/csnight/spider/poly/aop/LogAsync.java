package csnight.spider.poly.aop;

import java.lang.annotation.*;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAsync {
    String module() default "";

    String auth() default "";
}
