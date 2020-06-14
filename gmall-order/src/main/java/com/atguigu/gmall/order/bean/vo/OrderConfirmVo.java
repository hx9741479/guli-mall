package com.atguigu.gmall.order.bean.vo;

import com.atguigu.gmall.order.api.entity.OrderItemVo;
import com.atguigu.gmall.ums.api.vo.UserAddressEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmVo { //结算页面的确认页面，用户确认信息无误后可提交订单

    //  收货地址列表
    private List<UserAddressEntity> addresses;

    //送货清单，根据购物车页面传递过来的skuIds查询
    private List<OrderItemVo> items;

    //  用户的购物积分信息，ums_user表中的integration字段
    private Integer bounds;

    //  防重的唯一标识
    private String orderToken;

}
