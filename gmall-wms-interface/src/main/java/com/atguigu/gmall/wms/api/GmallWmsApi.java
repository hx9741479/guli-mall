package com.atguigu.gmall.wms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallWmsApi {

    /**
     *  5.根据skuId查询商品的库存信息 O
     * @param skuId
     * @return
     */
    @GetMapping("wms/waresku/sku/{skuId}")
    ResponseVo<List<WareSkuEntity>> getWare(@PathVariable("skuId") Long skuId);

    /**
     *  检查并锁定库存，使用分布式锁保证操作的原子性
     * @param lockVOS
     * @return
     */
    @PostMapping("wms/waresku/check/lock")
    ResponseVo<List<SkuLockVO>> checkAndLock(@RequestBody List<SkuLockVO> lockVOS);


    }
