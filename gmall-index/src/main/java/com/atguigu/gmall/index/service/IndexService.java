package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IndexService {

    List<CategoryEntity> queryLvl1Categories();

    List<CategoryEntity> queryLvl2CategoriesWithSub(Long pid);

    void testLock();
}
