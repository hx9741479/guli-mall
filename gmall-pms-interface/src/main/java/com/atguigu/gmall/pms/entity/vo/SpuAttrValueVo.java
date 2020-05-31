package com.atguigu.gmall.pms.entity.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
public class SpuAttrValueVo extends SpuAttrValueEntity {

    //商品基本属性的值集合，接收json值实质是调用了setter方法
    public void setValueSelected(List<String> valueSelected) {
        //空则不设置
        if (CollectionUtils.isEmpty(valueSelected)) {
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected, ","));
    }

}
