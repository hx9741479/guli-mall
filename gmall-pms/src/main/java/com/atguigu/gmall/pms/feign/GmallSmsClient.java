package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.pms.feign.fallback.GmallSmsClientFallBack;
import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "sms-service",fallback = GmallSmsClientFallBack.class)
public interface GmallSmsClient extends GmallSmsApi {

}
