package com.atguigu.gmall.order.service;

import com.atguigu.gmall.order.vo.OrderConfirmVo;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {

    OrderConfirmVo confirm();

}
