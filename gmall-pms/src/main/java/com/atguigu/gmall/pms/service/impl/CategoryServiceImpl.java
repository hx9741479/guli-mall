package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategory(Long parentId) {
        QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper<>();
        // 如果parentId为-1，说明用户没有传该字段，查询所有
        List<CategoryEntity> categoryEntities;
        if(parentId == -1){
            categoryEntities = this.list();
            return categoryEntities;
        }
        queryWrapper.eq("parent_id",parentId);
        categoryEntities = baseMapper.selectList(queryWrapper);
        return categoryEntities;
    }

    @Override
    public List<CategoryEntity> queryCategoriesWithSub(Long pid) {
       List<CategoryEntity> categoryEntityList =  this.baseMapper.queryCategoriesByPid(pid);
        return categoryEntityList;
    }

    /**
     *  根据sku中的三级分类id查询1，2，3级分类
     * @param cid3 
     * @return
     */
    @Override
    public List<CategoryEntity> queryCategoriesByCid3(Long cid3) {
        //查询三级分类
        CategoryEntity categoryEntity3 = this.baseMapper.selectById(cid3);
        //查询二级分类
        CategoryEntity categoryEntity2 = this.baseMapper.selectById(categoryEntity3.getParentId());
        //查询三级分类
        CategoryEntity categoryEntity1 = this.baseMapper.selectById(categoryEntity2.getParentId());
        return Arrays.asList(categoryEntity1,categoryEntity2,categoryEntity3);
    }
}