package com.kevin.rpc.spring.starter.common;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface EasyRpcService {
    //限流
    int limit() default 0;

    //服务分组
    String group() default "default";

    //令牌校验
    String serviceToken() default "";
}