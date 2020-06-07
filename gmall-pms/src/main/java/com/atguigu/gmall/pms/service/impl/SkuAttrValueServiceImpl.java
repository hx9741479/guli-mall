package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySearchAttrValueBySkuId(Long skuId) {
        return this.skuAttrValueMapper.querySearchAttrValueBySkuId(skuId);
    }

    @Override
    public List<SaleAttrValueVo> querySkuAttrValuesBySpuId(Long spuId) {
        List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueMapper.querySearchAttrValueBySpuId(spuId);
        //把返回的集合转化为我们想要的数据形式：
        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
        //以attrId进行分组
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));
        map.forEach((attrId, attrs) -> {
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            //attrId
            saleAttrValueVo.setAttrId(attrId);
            //attrName
            saleAttrValueVo.setAttrName(attrs.get(0).getAttrName());
            //attrValues 用set集合去重
            Set<String> attrValues = attrs.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet());
            saleAttrValueVo.setAttrValues(attrValues);
            saleAttrValueVos.add(saleAttrValueVo);
        });
        return saleAttrValueVos;
    }

    /**
     *  根据sku中的spuId查询spu下所有的sku: 销售属性组合与skuId映射关系
     * @param spuId
     * @return
     */
    @Override
    public String querySkusJsonBySpuId(Long spuId) {
        // [{"sku_id": 3, "attr_values": "暗夜黑,12G,512G"}, {"sku_id": 4, "attr_values": "白天白,12G,512G"}]
        List<Map<String, Object>> skus = this.skuAttrValueMapper.querySkusJsonBySpuId(spuId);
        // 转换成：{'暗夜黑,12G,512G': 3, '白天白,12G,512G': 4}
        Map<String, Long> map = skus.stream().collect(Collectors.toMap(sku -> sku.get("attr_values").toString(), sku -> (Long) sku.get("sku_id")));
        return JSON.toJSONString(map);
    }

    /**
     *  根据skuId获取sku销售属性对象集合
     * @param skuId
     * @return
     */
    @Override
    public List<SkuAttrValueEntity> querySkusBySkuId(Long skuId) {
        List<SkuAttrValueEntity> skuAttrValueEntityList = this.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId));
        return skuAttrValueEntityList;
    }
}