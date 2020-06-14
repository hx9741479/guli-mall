package com.atguigu.gmall.order.service;

import com.atguigu.gmall.order.api.entity.OrderEntity;
import com.atguigu.gmall.order.api.entity.OrderSubmitVO;
import com.atguigu.gmall.order.bean.vo.OrderConfirmVo;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {

    OrderConfirmVo confirm();

    OrderEntity submit(OrderSubmitVO submitVo);
}
