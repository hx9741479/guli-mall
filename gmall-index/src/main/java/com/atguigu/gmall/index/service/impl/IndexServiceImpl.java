package com.atguigu.gmall.index.service.impl;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    private GmallPmsApi gmallPmsApi;

    @Override
    public List<CategoryEntity> queryLvl1Categories() {
        //传0表示查询所有一级分类
        ResponseVo<List<CategoryEntity>> listResponseVo = this.gmallPmsApi.queryCategory(0l);
        return listResponseVo.getData();
    }

    //获取二级分类以其下的三级分类
    @Override
    public List<CategoryEntity> queryLvl2CategoriesWithSub(Long pid) {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.gmallPmsApi.queryCategoriesWithSub(pid);
        List<CategoryEntity> categoryEntityList = listResponseVo.getData();
        return categoryEntityList;
    }

}
