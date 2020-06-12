package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "cart-service")
public interface GmallCartClient extends GmallCartApi {

}
