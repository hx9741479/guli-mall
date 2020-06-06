package com.atguigu.gmall.index.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {
    //@GmallCache(prefix = "index:cates:",timeout= 43200,random = 3600,lock="lock"‬‬)
    /**
     *  缓存的前缀
     */
    String prefix() default "";

    /**
     * 设置缓存的有效时间
     *  单位：分钟
     */
    int timeout() default 5;

    /**
     *  防止雪崩设置的随机值范围
     */
    int random() default 5;

    /**
     *  防止击穿，分布式锁的key
     */
    String lock() default "lock";

}
