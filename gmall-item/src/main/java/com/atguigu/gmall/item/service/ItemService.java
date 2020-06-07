package com.atguigu.gmall.item.service;

import com.atguigu.gmall.item.vo.ItemVo;
import org.springframework.stereotype.Service;

@Service
public interface ItemService {
    ItemVo load(Long skuId);
}
