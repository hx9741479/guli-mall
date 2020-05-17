package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商品属性
 *
 * @author hhxx
 * @email hx9741479@139.com
 * @date 2020-05-17 14:57:07
 */
public interface AttrService extends IService<AttrEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

