package com.atguigu.gmall.wms.mapper;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author hhxx
 * @email hx9741479@139.com
 * @date 2020-05-17 22:53:53
 */
@Mapper
public interface WareSkuMapper extends BaseMapper<WareSkuEntity> {
    // 验库存的方法
    List<WareSkuEntity> checkStock(@Param("skuId") Long skuId,@Param("count") Integer count);

    // 锁库存
    int lockStock(@Param("skuId") Long skuId,@Param("count") Integer count);

    // 解库存
    int unLockStock(@Param("skuId") Long skuId, @Param("count")Integer count);

}
