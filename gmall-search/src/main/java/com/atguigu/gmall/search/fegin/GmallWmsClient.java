package com.atguigu.gmall.search.fegin;

import com.atguigu.gmall.search.fegin.fallback.GmallWmsFallBack;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "wms-service",fallback = GmallWmsFallBack.class)
public interface GmallWmsClient extends GmallWmsApi {
}
