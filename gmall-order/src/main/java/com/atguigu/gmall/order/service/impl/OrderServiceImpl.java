package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.api.entiy.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.order.api.entity.OrderEntity;
import com.atguigu.gmall.order.api.entity.OrderItemVo;
import com.atguigu.gmall.order.api.entity.OrderSubmitVO;
import com.atguigu.gmall.order.bean.UserInfo;
import com.atguigu.gmall.order.bean.vo.OrderConfirmVo;
import com.atguigu.gmall.order.exception.OrderException;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.api.vo.UserAddressEntity;
import com.atguigu.gmall.ums.api.vo.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private GmallCartClient cartClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallOmsClient omsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String KEY_PREFIX = "order:token:";

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     *  订单确认页
     *  由于存在大量的远程调用，使用异步编排做优化
     * @return
     */
    @Override
    public OrderConfirmVo confirm() {
        //  需要OrderConfirmVo数据，咱就new一个，然后把需要的值赋给他
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        //从拦截器获取用户id
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();

        //查询送货清单
        CompletableFuture<List<Cart>> cartCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //  获取已选中的购物车中的商品
            List<Cart> carts = this.cartClient.queryCheckedCarts(userId).getData();
            if(CollectionUtils.isEmpty(carts)){
                throw new OrderException("没有选中的购物车信息!");
            }
            return carts;
        }, threadPoolExecutor);

        //  获取送货清单中的商品赋值
        CompletableFuture<Void> itemCompletableFuture = cartCompletableFuture.thenAcceptAsync(carts -> {
           List<OrderItemVo> orderItemVos = carts.stream().map(cart -> {
               OrderItemVo orderItemVo = new OrderItemVo();
                orderItemVo.setSkuId(cart.getSkuId());
                orderItemVo.setCount(cart.getCount().intValue());
                //  根据skuId查询sku
               CompletableFuture<Void> skuCompletableFuture = CompletableFuture.runAsync(() -> {
                   SkuEntity skuEntity = this.pmsClient.querySkuById(cart.getSkuId()).getData();
                   orderItemVo.setTitle(skuEntity.getTitle());
                   orderItemVo.setDefaultImage(skuEntity.getDefaultImage());
                   orderItemVo.setPrice(skuEntity.getPrice());
                   orderItemVo.setWeight(new BigDecimal(skuEntity.getWeight()));
               }, threadPoolExecutor);
               //查询销售属性
               CompletableFuture<Void> saleAttrCompletableFuture = CompletableFuture.runAsync(() -> {
                   List<SkuAttrValueEntity> skuAttrValueEntityList = this.pmsClient.querySkuAttrValuesBySkuId(cart.getSkuId()).getData();
                   orderItemVo.setSaleAttrs(skuAttrValueEntityList);
               }, threadPoolExecutor);
               // 查询营销信息
               CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
                   List<ItemSaleVo> itemSaleVoList = this.smsClient.querySalesBySkuId(cart.getSkuId()).getData();
                   orderItemVo.setSales(itemSaleVoList);
               }, threadPoolExecutor);
               // 查询库存信息
               CompletableFuture<Void> wareCompletableFuture = CompletableFuture.runAsync(() -> {
                   List<WareSkuEntity> wareSkuEntityList = this.wmsClient.getWare(cart.getSkuId()).getData();
                   if(!CollectionUtils.isEmpty(wareSkuEntityList)){
                       boolean store = wareSkuEntityList.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0);
                       orderItemVo.setStore(store);
                   }
               }, threadPoolExecutor);

               // 保证上述操作全部执行完毕
               CompletableFuture.allOf(
                       wareCompletableFuture,
                       salesCompletableFuture,
                       saleAttrCompletableFuture,
                       skuCompletableFuture
               ).join();
               return orderItemVo;
           }).collect(Collectors.toList());
           orderConfirmVo.setItems(orderItemVos);
        }, threadPoolExecutor);

        // 查询收货地址列表
        CompletableFuture<Void> addressCompletableFuture = CompletableFuture.runAsync( () -> {
            List<UserAddressEntity> userAddressEntityList = this.umsClient.queryAddressesByUserId(userId).getData();
            orderConfirmVo.setAddresses(userAddressEntityList);
        }, threadPoolExecutor);

        // 查询用户的积分信息
        CompletableFuture<Void> boundsCompletableFuture = CompletableFuture.runAsync(() -> {
            UserEntity userEntity = this.umsClient.queryUserById(userId).getData();
            if(userEntity != null){
                orderConfirmVo.setBounds(userEntity.getIntegration());
            }
        }, threadPoolExecutor);
        //防重的唯一标识
        CompletableFuture<Void> orderTokenCompletableFuture = CompletableFuture.runAsync(() -> {
            String timeId = IdWorker.getTimeId();
            this.redisTemplate.opsForValue().set(KEY_PREFIX + timeId,timeId);
            orderConfirmVo.setOrderToken(timeId);
        }, threadPoolExecutor);

        CompletableFuture.allOf(
                orderTokenCompletableFuture,
                boundsCompletableFuture,
                addressCompletableFuture,
                itemCompletableFuture
        ).join();

        return orderConfirmVo;
    }

    /**
     *  提交订单
     * @param submitVo
     * @return
     */
    @Override
    public OrderEntity submit(OrderSubmitVO submitVo) {
        //1 防重
        String orderToken = submitVo.getOrderToken();
        //使用lua脚本保证验证和删除的原子性
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then return redis.call('del', KEYS[1]) " +
                "else return 0 end";
        Boolean flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(KEY_PREFIX + orderToken), orderToken);
        if(!flag){
            throw new OrderException("您多次提交过快，请稍后再试！");
        }
        //2 验价
        BigDecimal totalPrice = submitVo.getTotalPrice(); // 获取页面上的价格
        List<OrderItemVo> items = submitVo.getItems();// 订单详情
        if(CollectionUtils.isEmpty(items)){
            throw new OrderException("您没有选中商品，请选择要购买的商品！");
        }
        // 遍历订单详情，获取数据库价格，计算实时总价
        BigDecimal currentTotalPrice = items.stream().map(item -> {
            SkuEntity skuEntity = this.pmsClient.querySkuById(item.getSkuId()).getData();
            if (skuEntity != null) {
                return skuEntity.getPrice().multiply(new BigDecimal(item.getCount()));
            }
            return new BigDecimal(0);
        }).reduce((t1, t2) -> t1.add(t2)).get();
        if(totalPrice.compareTo(currentTotalPrice) != 0){
            throw new OrderException("页面已过期，刷新后再试！");
        }
        //3 验库存并锁库存
        List<SkuLockVO> lockVOS = items.stream().map(item -> {
            SkuLockVO skuLockVO = new SkuLockVO();
            skuLockVO.setSkuId(item.getSkuId());
            skuLockVO.setCount(item.getCount().intValue());
            skuLockVO.setOrderToken(submitVo.getOrderToken());
            return skuLockVO;
        }).collect(Collectors.toList());
        ResponseVo<List<SkuLockVO>> skuLockResp = this.wmsClient.checkAndLock(lockVOS);
        List<SkuLockVO> skuLockVOS = skuLockResp.getData();
        if (!CollectionUtils.isEmpty(skuLockVOS)){
            throw new OrderException("手慢了，商品库存不足：" + JSON.toJSONString(skuLockVOS));
        }

        //order: 此时服务器宕机

        //4 下单
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        OrderEntity orderEntity = null;
        try {
            orderEntity = this.omsClient.saveOrder(submitVo, userId).getData();//fegin 响应超时
        } catch (Exception e) {
            e.printStackTrace();
            //如果顶单创建失败，立马释放库存
            //this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE","stock.unlock",orderToken);
        }

        //5 删除购物车，异步发送消息给购物车，删除购物车
        Map<String, Object> map = new HashMap<>();
        //删除购物车需要的数据
        map.put("userId",userId);
        List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        map.put("skuIds",JSON.toJSONString(skuIds));
        //this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE","cart.delete",map);

        return orderEntity;
    }
}
