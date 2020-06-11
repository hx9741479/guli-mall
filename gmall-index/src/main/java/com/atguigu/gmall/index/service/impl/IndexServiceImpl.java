package com.atguigu.gmall.index.service.impl;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private GmallPmsApi gmallPmsApi;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;


    public static final String KEY_PREFIX = "index:category:";


    @GmallCache(prefix = KEY_PREFIX + 0)
    @Override
    public List<CategoryEntity> queryLvl1Categories() {
        //传0表示查询所有一级分类
        ResponseVo<List<CategoryEntity>> listResponseVo = this.gmallPmsApi.queryCategory(0l);
        List<CategoryEntity> categoryEntityList = listResponseVo.getData();
        return categoryEntityList;
    }

    //获取二级分类以其下的三级分类
    @GmallCache(prefix = KEY_PREFIX,timeout = 43200,random = 3600,lock = "lock")
    @Override
    public List<CategoryEntity> queryLvl2CategoriesWithSub(Long pid) {
        //专注于业务，缓存，防止缓存穿透使用SpringAOP，交给@GmallCache
        ResponseVo<List<CategoryEntity>> listResponseVo = this.gmallPmsApi.queryCategoriesWithSub(pid);
        List<CategoryEntity> categoryEntityList = listResponseVo.getData();
        return categoryEntityList;
    }

    @Override
    public void testLock() {
        //获取锁，只要锁名一样就是同一把锁
        RLock lock = redissonClient.getLock("lock");
        lock.lock(10,TimeUnit.SECONDS);
        //查询rdis中的num的值
            String num = this.redisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(num)) {
                return;
            }
            int numInt = Integer.parseInt(num);
            this.redisTemplate.opsForValue().set("num", String.valueOf(++numInt));
        //解锁
        lock.unlock();

    }

    @Override
    public String readLock() {
        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("countdown");
        try {
            countDownLatch.trySetCount(6);
            countDownLatch.await();

            return "关门了。。。。。";
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String writeLock() {
        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("countdown");

        countDownLatch.countDown();
        return "出来了一个人。。。";
    }
}
