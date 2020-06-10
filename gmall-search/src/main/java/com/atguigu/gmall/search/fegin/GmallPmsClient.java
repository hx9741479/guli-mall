package com.atguigu.gmall.search.fegin;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.search.fegin.fallback.GmallPmsFallBack;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "pms-service",fallback = GmallPmsFallBack.class)
public interface GmallPmsClient extends GmallPmsApi {

}
