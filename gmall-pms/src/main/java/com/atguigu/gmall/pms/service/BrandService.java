package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 品牌
 *
 * @author hhxx
 * @email hx9741479@139.com
 * @date 2020-05-17 14:57:07
 */
public interface BrandService extends IService<BrandEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

