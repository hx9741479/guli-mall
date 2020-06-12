package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.api.entiy.Cart;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CartAsyncService {

    @Autowired
    private CartMapper cartMapper;

    /**
     *  根据user_id和sku_id更新购物车
     * @param cart
     */
    @Async
    public void updateCartByUserIdAndSkuId(Cart cart){
        this.cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", cart.getUserId()).eq("sku_id", cart.getSkuId()));
    }

    /**
     *  保存购物车
     * @param cart
     */
    @Async
    public void saveCart(Cart cart){
        this.cartMapper.insert(cart);
    }

    @Async
    public void deleteCartsByUserId(String userKey) {
        this.cartMapper.delete(new QueryWrapper<Cart>().eq("user_id",userKey));
    }

    @Async
    public void deleteByUserIdAndSkuId(String userId, Long skuId) {
        this.cartMapper.delete(new QueryWrapper<Cart>().eq("user_id",userId).eq("sku_id",skuId));
    }
}
