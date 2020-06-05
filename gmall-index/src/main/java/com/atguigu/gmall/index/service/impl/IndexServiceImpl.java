package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
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

    public static final String KEY_PREFIX = "index:category:";


    @Override
    public List<CategoryEntity> queryLvl1Categories() {

        //查询缓存
        String categories = redisTemplate.opsForValue().get(KEY_PREFIX + 0);
        //缓存中有，则直接命中
        if(StringUtils.isNotBlank(categories)){
            List<CategoryEntity> categoryEntities = JSON.parseArray(categories, CategoryEntity.class);
            return categoryEntities;
        }
        //缓存中无，则查询数据库，并存入放入缓存
        //传0表示查询所有一级分类
        ResponseVo<List<CategoryEntity>> listResponseVo = this.gmallPmsApi.queryCategory(0l);
        List<CategoryEntity> categoryEntityList = listResponseVo.getData();
        redisTemplate.opsForValue().set(KEY_PREFIX + 0,JSON.toJSONString(categoryEntityList),30,TimeUnit.DAYS);
        return categoryEntityList;
    }

    //获取二级分类以其下的三级分类
    @Override
    public List<CategoryEntity> queryLvl2CategoriesWithSub(Long pid) {
        //1、从缓存中获取
        String cacheCategories = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        //2、如果缓存中有则直接命中
        if(StringUtils.isNotBlank(cacheCategories)){
            List<CategoryEntity> categoryEntities = JSON.parseArray(cacheCategories, CategoryEntity.class);
            return categoryEntities;
        }
        ResponseVo<List<CategoryEntity>> listResponseVo = this.gmallPmsApi.queryCategoriesWithSub(pid);
        //2.1 将查询结果放入缓存
        List<CategoryEntity> categoryEntityList = listResponseVo.getData();
        this.redisTemplate.opsForValue().set(KEY_PREFIX + pid,JSON.toJSONString(categoryEntityList),30, TimeUnit.DAYS);
        return categoryEntityList;
    }

    @Override
    public synchronized void testLock() {
        // 查询redis中的num值
        String value = this.redisTemplate.opsForValue().get("num");
        // 没有该值return
        if (StringUtils.isBlank(value)) {
            return;
        }
        // 有值就转成成int
        int num = Integer.parseInt(value);
        // 把redis中的num值+1
        this.redisTemplate.opsForValue().set("num", String.valueOf(++num));
    }

}
