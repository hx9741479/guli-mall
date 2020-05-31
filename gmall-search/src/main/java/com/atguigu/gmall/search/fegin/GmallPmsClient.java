package com.atguigu.gmall.search.fegin;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "pms-service")
public interface GmallPmsClient extends GmallPmsApi {

}
