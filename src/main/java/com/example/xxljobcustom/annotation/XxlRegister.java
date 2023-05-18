package com.example.xxljobcustom.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 来配合原生的@XxlJob注解进行使用，填写这几个字段的信息：
 * 默认调度类型为CRON、运行模式为BEAN
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface XxlRegister {
    String cron();
    String jobDesc() default "default jobDesc";
    String author() default "default Author";
    /*
     * 默认为 ROUND 轮询方式
     * 可选： FIRST 第一个
     * */
    String executorRouteStrategy() default "ROUND";
    int triggerStatus() default 0;
}
