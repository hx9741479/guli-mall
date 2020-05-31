package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallPmsApi {

    //1.分页查询spu V O
    @PostMapping("pms/spu/page")
    ResponseVo<List<SpuEntity>> querySpuPage(@RequestBody PageParamVo pageParamVo);
    //2.根据spuId查询sku信息 O
    @GetMapping("pms/sku/spu/{spuId}")
    ResponseVo<List<SkuEntity>> getSku(@PathVariable("spuId")Long spuId);
    //3.根据brandId查询品牌信息 O
    @GetMapping("pms/brand/{id}")
    ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);
    //4.根据分类的id查询分类信息 O
    @GetMapping("pms/category/{id}")
    ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);
    //6.查询销售搜索类型的规格参数以及值 V
    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValueBySkuId(@PathVariable("skuId")Long skuId);
    //7.查询基本搜索类型的规格参数以及值 V
    @GetMapping("pms/spuattrvalue/spu/{spuId}")
    ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValueBySpuId(@PathVariable Long spuId);

    }
