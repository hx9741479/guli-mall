package com.atguigu.gmall.index.config;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     *  joinPoint.getArgs(): 获取方法参数
     *  joinPoint.getTarget().getClass(): 获取目标类
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    //@Around("@annotation(com.atguigu.gmall.index.config.GmallCache)")
    @Around("@annotation(com.atguigu.gmall.index.config.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        //获取切点方法的签名
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        //获取方法对象
        Method method = signature.getMethod();
        //获取方法上指定注解的对象
        GmallCache annotation = method.getAnnotation(GmallCache.class);
        //获取注解中的前缀
        String prefix = annotation.prefix();
        //获取方法中的参数
        Object[] args = joinPoint.getArgs();
        String param = Arrays.asList(args).toString();//参数一个为3，则得到：[3]
        //获取方法的返回值类型
        Class<?> returnType = method.getReturnType();

        // 1、拦截前代码块：判断缓存中有没有
        String json = this.redisTemplate.opsForValue().get(prefix + param);
        //命中则直接返回
        if (StringUtils.isNotBlank(json)){
            return JSON.parseObject(json,returnType);
        }
        // 2、没有，加分布式锁
        String lock = annotation.lock();
        RLock rLock = this.redissonClient.getLock(lock + param);
        rLock.lock();
        // 2.1、判断缓存中有没有，有直接返回(加锁的过程中，别的请求可能已经把数据放入缓存)
        String json2 = this.redisTemplate.opsForValue().get(prefix + param);
        // 2.1.1 判断缓存中的数据是否为空，非空则返回结果
        if (StringUtils.isNotBlank(json2)){
            rLock.unlock();
            return JSON.parseObject(json2,returnType);
        }
        // 4、执行目标方法
        Object result = joinPoint.proceed(joinPoint.getArgs());
        // 5、拦截后代码块：放入缓存 释放分布锁
        int timeout = annotation.timeout();
        //过期时间加随机值，防止缓存雪崩
        //获取注解内指定的随机值范围
        int randomScope = annotation.random();
        int random = new Random().nextInt(randomScope);
        this.redisTemplate.opsForValue().set(prefix + param,JSON.toJSONString(result),timeout + random, TimeUnit.MINUTES);
        rLock.unlock();
        return result;
    }

}
