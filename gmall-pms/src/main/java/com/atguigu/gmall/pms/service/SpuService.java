package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.entity.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.FileNotFoundException;

/**
 * spu信息
 *
 * @author hhxx
 * @email hx9741479@139.com
 * @date 2020-05-17 14:57:07
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo querySpuInfo(PageParamVo pageParamVo, Long categoryId);

    void bigSave(SpuVo spuVo) throws FileNotFoundException;

    //bigSave抽取出来的方法，第二次测试把saveSpuDesc放到spuDescService中去，测试事务传播行为
    Long saveSpu(SpuVo spuVo);

    void saveSpuAttr(SpuVo spuVo,Long spuId);
    void saveSku(SpuVo spuVo,Long spuId);
    //end

}

