package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.api.entiy.Cart;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;

@Service
public interface CartService {
    Cart queryCartBySkuId(Long skuId);

    void addCart(Cart cart);

   String executor2();

    ListenableFuture<String> executor1();

    List<Cart> queryCarts();

    void updateNum(Cart cart);

    void deleteCart(Long skuId);

    List<Cart> queryCheckedCarts(Long userId);

    //void scheduledTest();

}
