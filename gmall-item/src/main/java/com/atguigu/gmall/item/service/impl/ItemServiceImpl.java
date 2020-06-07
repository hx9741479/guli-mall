package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.entity.vo.ItemGroupVo;
import com.atguigu.gmall.pms.entity.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Override
    public ItemVo load(Long skuId) {
        ItemVo itemVo = new ItemVo();

        //1 根据skuId查询sku
        SkuEntity skuEntity = this.pmsClient.querySkuById(skuId).getData();
        itemVo.setSkuId(skuEntity.getId());
        itemVo.setTitle(skuEntity.getTitle());
        itemVo.setSubTitle(skuEntity.getSubtitle());
        itemVo.setPrice(skuEntity.getPrice());
        itemVo.setWeight(skuEntity.getWeight());
        itemVo.setDefaltImage(skuEntity.getDefaultImage());

        //2 根据sku中的三级分类id查询一二三级分类
        List<CategoryEntity> categoryEntityList = this.pmsClient.queryCategoriesByCid3(skuEntity.getCatagoryId()).getData();
        itemVo.setCategories(categoryEntityList);

        //3 根据sku中的品牌id查询品牌
        BrandEntity brandEntity = this.pmsClient.queryBrandById(skuEntity.getBrandId()).getData();
        itemVo.setBrandId(brandEntity.getId());
        itemVo.setBrandName(brandEntity.getName());

        //4 根据sku中的spuId查询spu信息
        SpuEntity spuEntity = this.pmsClient.querySpuById(skuEntity.getSpuId()).getData();
        itemVo.setSpuId(spuEntity.getId());
        itemVo.setSpuName(spuEntity.getName());

        //5 根据skuId查询sku所有的图片
        List<SkuImagesEntity> skuImagesEntityList = this.pmsClient.queryImagesBySkuId(skuId).getData();
        itemVo.setImages(skuImagesEntityList);

        //6 根据skuI查询sku的所有营销信息
        List<ItemSaleVo> itemSaleVoList = this.smsClient.querySalesBySkuId(skuId).getData();
        itemVo.setSales(itemSaleVoList);

        //7 根据skuId查询sku的库存信息
        List<WareSkuEntity> wareSkuEntityList = this.wmsClient.getWare(skuId).getData();
        if (!CollectionUtils.isEmpty(wareSkuEntityList)) {
            itemVo.setStore(
                    wareSkuEntityList.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0)
            );
        }

        //8 根据sku中的spuId查询spu下的所有销售属性
        List<SaleAttrValueVo> saleAttrValueVoList = this.pmsClient.querySkuAttrValuesBySpuId(skuEntity.getSpuId()).getData();
        itemVo.setSaleAttrs(saleAttrValueVoList);

        //9 根据skuId查询当前sku的销售属性
        List<SkuAttrValueEntity> skuAttrValueEntityList = this.pmsClient.querySkusBySkuId(skuId).getData();
        //转换需要的数据集合
        Map<Long, String> map = skuAttrValueEntityList.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
        itemVo.setSaleAttr(map);

        //10 根据sku中的spuId查询spu下所有sku: 销售属性组合与skuId映射关系
        String skusJson = this.pmsClient.querySkusJsonBySpuId(skuEntity.getSpuId()).getData();
        itemVo.setSkusJson(skusJson);

        //11 根据sku中的spuId查询spu的描述信息,查询spu的海报信息
        SpuDescEntity spuDescEntity = this.pmsClient.querySpuDescById(skuEntity.getSpuId()).getData();
        if (spuDescEntity != null) {
            String decript = spuDescEntity.getDecript();
            //split分割时，当没有“，”时候，即只有一个单位，如果还进行分割会包异常，所以判断有无分割字符
            if (!decript.contains(",")) {
                itemVo.setSpuImages(Arrays.asList(decript));
            } else {
                String[] imgesArray = StringUtils.split(decript, ",");
                itemVo.setSpuImages(Arrays.asList(imgesArray));
            }
        }

        //12 根据分类id、spuId及skuId查询分组及组下的规格参数值
        List<ItemGroupVo> itemGroupVoList = this.pmsClient.queryGroupsBySpuIdAndCid(skuEntity.getSpuId(), skuId, skuEntity.getCatagoryId()).getData();
        itemVo.setGroups(itemGroupVoList);

        return itemVo;
    }
}
