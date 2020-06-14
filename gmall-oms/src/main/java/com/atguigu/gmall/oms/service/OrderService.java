package com.atguigu.gmall.oms.service;

import com.atguigu.gmall.order.api.entity.OrderSubmitVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.order.api.entity.OrderEntity;

/**
 * 订单
 *
 * @author hhxx
 * @email hx9741479@139.com
 * @date 2020-05-17 23:11:46
 */
public interface OrderService extends IService<OrderEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    OrderEntity saveOrder(OrderSubmitVO orderSubmitVO, Long userId);
}

