package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.entity.vo.SkuVo;
import com.atguigu.gmall.pms.entity.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.entity.vo.SpuVo;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.service.SpuService;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private SpuDescMapper spuDescMapper;

    @Autowired
    private SpuAttrValueService spuAttrValueService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuInfo(PageParamVo pageParamVo, Long categoryId) {
        //封装查询条件
        QueryWrapper<SpuEntity> queryWrapper = new QueryWrapper<>();

        //是否根据分类id查询
        if (categoryId != 0) {
            queryWrapper.eq("category_id", categoryId);
        }
        ///如果用户输入了检索条件，根据检索条件查
        String key = pageParamVo.getKey();
        if (StringUtils.isNotBlank(key)) {
            //and相当在里面的条件外面加个括号
            queryWrapper.and(wrapper -> wrapper.like("id", key).or().like("name", key));
        }
        return new PageResultVo(
                this.page(pageParamVo.getPage(), queryWrapper)
        );
    }

    @Override
    public void bigSave(SpuVo spuVo) {
        // 1、保存sup信息
        // 1.1. 保存spu基本信息 pms_spu
        spuVo.setPublishStatus(1);//默认是已上架
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime());//新增时，更新时间和创建时间一致
        this.save(spuVo);
        Long spuId = spuVo.getId();// 获取新增后的spuId，供关联表使用

        // 1.2. 保存spu的描述信息 spu_desc
        SpuDescEntity spuDescEntity = new SpuDescEntity();
        // 注意：spu_id表的主键是spu_id,需要在实体类中配置该主键不是自增主键
        spuDescEntity.setSpuId(spuId);
        // 把商品的图片描述，保存到spu详情中，图片地址以逗号进行分割
        spuDescEntity.setDecript(StringUtils.join(spuVo.getSpuImages(), ","));
        this.spuDescMapper.insert(spuDescEntity);

        // 1.3. 保存spu的规格参数信息
        List<SpuAttrValueVo> spuAttrValueVos = spuVo.getBaseAttrs();
        //转换成 SpuAttrValueEntity 集合
        List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueVos.stream().map(spuAttrValueVo -> {
            SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
            spuAttrValueEntity.setSpuId(spuId);
            spuAttrValueEntity.setAttrId(spuAttrValueVo.getAttrId());
            spuAttrValueEntity.setAttrName(spuAttrValueVo.getAttrName());
            spuAttrValueEntity.setSort(0);
            spuAttrValueEntity.setAttrValue(spuAttrValueVo.getAttrValue());
            return spuAttrValueEntity;
        }).collect(Collectors.toList());
        spuAttrValueService.saveBatch(spuAttrValueEntities);

        // 2. 保存sku相关信息，sku相关pms_sku、pms_attr_value、pms_sku_images、其他两张表保存时
        //需要pms_sku保存时的sku_id所以每次保存sku_id时都要保存其他两张表
        List<SkuVo> skus = spuVo.getSkus();
        if (CollectionUtils.isEmpty(skus)) {
            return;
        }
        skus.forEach(skuVo -> {
            // 2.1. 保存sku基本信息
            SkuEntity skuEntity = new SkuEntity();
            BeanUtils.copyProperties(skuVo, skuEntity);
            // 品牌和分类的id需要从spuVo中获取
            skuEntity.setCatagoryId(spuVo.getCategoryId());
            skuEntity.setBrandId(spuVo.getBrandId());
            // 获取图片列表
            List<String> images = skuVo.getImages();
            // 如果图片列表不为null，则设置默认图片
            if (!CollectionUtils.isEmpty(images)) {
                // 设置第一张图片作为默认图片(现在前端没有穿默认图片，但是我们要有前瞻性：可能以后会扩展，前端会传一个默认图片）
                skuEntity.setDefaultImage(skuEntity.getDefaultImage() == null ? images.get(0) : skuEntity.getDefaultImage());
            }
            skuEntity.setSpuId(spuId);
            //保存一条sku基本信息
            this.skuMapper.insert(skuEntity);
            //获取skuId
            Long skuId = skuEntity.getId();
            // 2.2. 保存sku图片信息
            if (!CollectionUtils.isEmpty(images)) {
                String defaultImage = skuEntity.getDefaultImage() == null ? images.get(0) : skuEntity.getDefaultImage();
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setUrl(image);
                    skuImagesEntity.setSort(0);
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(defaultImage, image) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(skuImagesEntities);
            }
            // 2.3. 保存sku的规格参数（销售属性）
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            saleAttrs.forEach(saleAttr -> {
                saleAttr.setSkuId(skuId);
                saleAttr.setSort(0);
            });
            this.skuAttrValueService.saveBatch(saleAttrs);
            // 3. 保存营销相关信息，需要远程调用gmall-sms
            // 3.1. 积分优惠
            // 3.2. 满减优惠
            // 3.3. 数量折扣
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo,skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            gmallSmsClient.saveSkuSaleInfo(skuSaleVo);
        });

    }

}