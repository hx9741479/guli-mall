//package com.atguigu.gmall.pms.feign.feign.fallback;
//
//import com.atguigu.gmall.common.bean.ResponseVo;
//import com.atguigu.gmall.pms.feign.GmallSmsClient;
//import com.atguigu.gmall.sms.vo.SkuSaleVo;
//
//public class GmallSmsClientFallBack implements GmallSmsClient {
//    @Override
//    public ResponseVo<Object> saveSkuSaleInfo(SkuSaleVo skuSaleVo) {
//        return ResponseVo.ok("熔断保护");
//    }
//}
