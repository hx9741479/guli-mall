package com.atguigu.gmall.search.fegin.fallback;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.fegin.GmallWmsClient;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;

import java.util.List;

public class GmallWmsFallBack implements GmallWmsClient {
    @Override
    public ResponseVo<List<WareSkuEntity>> getWare(Long skuId) {
        return ResponseVo.fail("熔断保护");
    }

    @Override
    public ResponseVo<List<SkuLockVO>> checkAndLock(List<SkuLockVO> lockVOS) {
        return null;
    }
}
