package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.api.gmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "sms-service")
public interface GmallSmsClient extends gmallSmsApi {

}
