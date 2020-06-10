package com.atguigu.gmall.search.fegin.fallback;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.entity.vo.ItemGroupVo;
import com.atguigu.gmall.pms.entity.vo.SaleAttrValueVo;
import com.atguigu.gmall.search.fegin.GmallPmsClient;

import java.util.List;

public class GmallPmsFallBack implements GmallPmsClient {
    @Override
    public ResponseVo<List<SpuEntity>> querySpuPage(PageParamVo pageParamVo) {
        return ResponseVo.fail("熔断保护");
    }

    @Override
    public ResponseVo<List<SkuEntity>> getSku(Long spuId) {
        return ResponseVo.fail("熔断保护");
    }

    @Override
    public ResponseVo<BrandEntity> queryBrandById(Long id) {
        return ResponseVo.fail("熔断保护");
    }

    @Override
    public ResponseVo<CategoryEntity> queryCategoryById(Long id) {
        return ResponseVo.fail("熔断保护");
    }

    @Override
    public ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValueBySkuId(Long skuId) {
        return ResponseVo.fail("熔断保护");
    }

    @Override
    public ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValueBySpuId(Long spuId) {
        return ResponseVo.fail("熔断保护");
    }

    @Override
    public ResponseVo<List<CategoryEntity>> queryCategory(Long parentId) {
        return ResponseVo.fail("熔断保护");
    }

    @Override
    public ResponseVo<List<CategoryEntity>> queryCategoriesWithSub(Long pid) {
        return ResponseVo.fail("熔断保护");
    }

    @Override
    public ResponseVo<SkuEntity> querySkuById(Long id) {
        return null;
    }

    @Override
    public ResponseVo<List<CategoryEntity>> queryCategoriesByCid3(Long cid3) {
        return null;
    }

    @Override
    public ResponseVo<List<SkuImagesEntity>> queryImagesBySkuId(Long skuId) {
        return null;
    }

    @Override
    public ResponseVo<List<SaleAttrValueVo>> querySkuAttrValuesBySpuId(Long spuId) {
        return null;
    }

    @Override
    public ResponseVo<String> querySkusJsonBySpuId(Long spuId) {
        return null;
    }

    @Override
    public ResponseVo<SpuDescEntity> querySpuDescById(Long spuId) {
        return null;
    }

    @Override
    public ResponseVo<SpuEntity> querySpuById(Long id) {
        return null;
    }

    @Override
    public ResponseVo<List<SkuAttrValueEntity>> querySkusBySkuId(Long skuId) {
        return null;
    }

    @Override
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValuesBySkuId(Long skuId) {
        return null;
    }

    @Override
    public ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValuesBySkuId(Long skuId) {
        return null;
    }

    @Override
    public ResponseVo<List<ItemGroupVo>> queryGroupsBySpuIdAndCid(Long spuId, Long skuId, Long cid) {
        return null;
    }
}
