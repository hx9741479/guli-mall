package com.atguigu.gmall.cart.api;

import com.atguigu.gmall.cart.api.entiy.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GmallCartApi {

    /**
     *  查询用户选中的购物车记录
     * @param userId
     * @return
     */
    @GetMapping("check/{userId}")
    ResponseVo<List<Cart>> queryCheckedCarts(@PathVariable("userId") Long userId);

}
