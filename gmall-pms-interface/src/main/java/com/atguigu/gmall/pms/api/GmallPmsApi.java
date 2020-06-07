package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.entity.vo.ItemGroupVo;
import com.atguigu.gmall.pms.entity.vo.SaleAttrValueVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    //1.分页查询spu V O
    @PostMapping("pms/spu/page")
    ResponseVo<List<SpuEntity>> querySpuPage(@RequestBody PageParamVo pageParamVo);

    //2.根据spuId查询sku信息 O
    @GetMapping("pms/sku/spu/{spuId}")
    ResponseVo<List<SkuEntity>> getSku(@PathVariable("spuId") Long spuId);


    /**
     * 3.根据brandId查询品牌信息 O
     *
     * @param id
     * @return
     */
    @GetMapping("pms/brand/{id}")
    ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    //4.根据分类的id查询分类信息 O
    @GetMapping("pms/category/{id}")
    ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    //6.查询销售搜索类型的规格参数以及值 V
    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValueBySkuId(@PathVariable("skuId") Long skuId);


    /**
     * 7.查询基本搜索类型的规格参数以及值 V
     *
     * @param spuId
     * @return
     */
    @GetMapping("pms/spuattrvalue/spu/{spuId}")
    ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValueBySpuId(@PathVariable Long spuId);

    //传0查询一级分类
    @GetMapping("pms/category/parent/{parentId}")
    ResponseVo<List<CategoryEntity>> queryCategory(@PathVariable("parentId") Long parentId);

    //查询二级分类以及二级分类下的三级分类
    @GetMapping("pms/category/subs/{pid}")
    ResponseVo<List<CategoryEntity>> queryCategoriesWithSub(@PathVariable("pid") Long pid);

    /**
     * 根据skuId查询sku
     *
     * @param id
     * @return
     */
    @GetMapping("pms/sku/{id}")
    ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    /**
     * 根据sku中的三级分类id查询一二三级分类
     *
     * @param cid3
     * @return
     */
    @GetMapping("pms/category/all/{cid3}")
    ResponseVo<List<CategoryEntity>> queryCategoriesByCid3(@PathVariable("cid3") Long cid3);

    /**
     * 根据skuId查询sku所有图片
     *
     * @param skuId
     * @return
     */
    @GetMapping("pms/skuimages/sku/{skuId}")
    ResponseVo<List<SkuImagesEntity>> queryImagesBySkuId(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    ResponseVo<List<SaleAttrValueVo>> querySkuAttrValuesBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/skuattrvalue/spu/sku/{spuId}")
    ResponseVo<String> querySkusJsonBySpuId(@PathVariable("spuId") Long spuId);

    //根据sku中的spuId查询spu的描述信息
    @GetMapping("pms/spudesc/{spuId}")
    ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    /**
     * 根据spuId查询spu的详细信息
     *
     * @param id
     * @return
     */
    @GetMapping("pms/spu/{id}")
    ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    /**
     * 根据skuId查询当前sku的销售属性
     *
     * @param skuId
     * @return
     */
    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValuesBySkuId(@PathVariable("skuId") Long skuId);

    /**
     * 根据cid3 spuId skuId 查询组及组下的规格参数
     *
     * @param spuId
     * @param skuId
     * @param cid
     * @return
     */
    @GetMapping("pms/attrgroup/withattrvalues")
    public ResponseVo<List<ItemGroupVo>> queryGroupsBySpuIdAndCid(
            @RequestParam("spuId") Long spuId,
            @RequestParam("skuId") Long skuId,
            @RequestParam("cid") Long cid);

    /**
     *  根据skuId获取sku集合以skuId分组，内有当前前sku的销售属性：{3:'白色',8:'8G',9:'128G'}
     * @param skuId
     * @return
     */
    @GetMapping("pms/skuattrvalue/spu/skus/{skuId}")
    ResponseVo<List<SkuAttrValueEntity>> querySkusBySkuId(@PathVariable("skuId") Long skuId);

}






