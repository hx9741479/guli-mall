package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.order.api.entity.OrderEntity;
import com.atguigu.gmall.order.api.entity.OrderSubmitVO;
import com.atguigu.gmall.order.bean.vo.OrderConfirmVo;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("confirm")
    public String confirm(Model model){

        OrderConfirmVo confirmVo = this.orderService.confirm();
        model.addAttribute("confirmVo", confirmVo);
        return "trade";
    }

    /**
     * 提交订单返回订单id
     * @param submitVo
     * @return
     */
    @PostMapping("submit")
    @ResponseBody
    public ResponseVo<Object> submit(@RequestBody OrderSubmitVO submitVo){

        OrderEntity orderEntity = this.orderService.submit(submitVo);
        return ResponseVo.ok(orderEntity.getOrderSn());
    }

}