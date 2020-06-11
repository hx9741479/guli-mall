package com.atguigu.gmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.bean.Cart;
import com.atguigu.gmall.cart.bean.UserInfo;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private CartAsyncService cartAsyncService;

    private static final String KEY_PREFIX = "cart:info:";

    /**
     * 根据skuId查询购物车是否有该商品
     *
     * @param skuId
     * @return
     */
    @Override
    public Cart queryCartBySkuId(Long skuId) {
        //1. 获取登陆信息
        String userId = getUserId();
        String key = KEY_PREFIX + userId;

        //2 获取redis中该用户的购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())) {
            String cartJson = hashOps.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson, Cart.class);
        }
        throw new RuntimeException("您的购物车中没有该商品的记录！");
    }

    @Override
    public void addCart(Cart cart) {
        //1 获取登陆信息
        String userId = getUserId();
        String key = KEY_PREFIX + userId;

        //2 获取redis中该用户的购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        //3 判断该用户的购物车信息是否已包含了该商品
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();// 用户添加购物车的商品数量
        if (hashOps.hasKey(skuId)) {
            //4 包含，更新数量
            String catJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(catJson, Cart.class);
            cart.setCount(cart.getCount().add(count));
            //使用异步调用，把数据同步到数据库
            //this.cartMapper.update(cart,new QueryWrapper<Cart>().eq("user_id",cart.getUserId()).eq("sku_id",cart.getSkuId()));
            this.cartAsyncService.updateCartByUserIdAndSkuId(cart);
        } else {
            //5 不包含，给用户新增购物车记录 skuId count
            cart.setUserId(userId);
            //调用远程接口，给cart赋值
            //根据skuId查询sku
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null) {
                cart.setTitle(skuEntity.getTitle());
                cart.setPrice(skuEntity.getPrice());
                cart.setImage(skuEntity.getDefaultImage());
            }
            //根据skuId查询销售属性
            List<SkuAttrValueEntity> skuAttrValueEntities = this.pmsClient.querySkuAttrValuesBySkuId(cart.getSkuId()).getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));

            //根据skuId查询营销信息
            List<ItemSaleVo> itemSaleVos = this.smsClient.querySalesBySkuId(cart.getSkuId()).getData();
            cart.setSales(JSON.toJSONString(itemSaleVos));

            // 根据skuId查询库存信息
            List<WareSkuEntity> wareSkuEntities = this.wmsClient.getWare(cart.getSkuId()).getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
            //商品刚加入购物车时，默认为选中状态
            cart.setCheck(true);
            //使用异步调用，把保存购物车到数据库
            //this.cartMapper.insert(cart);
            this.cartAsyncService.saveCart(cart);
        }
        //购物车商品更新，或商品添加到购物车
        hashOps.put(skuId, JSON.toJSONString(cart));
    }

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() != null) {
            //如果用户的id不为空，说明该用户已登录，添加购物车应该以userId作为key
            return userInfo.getUserId().toString();
        }
        //否则，说明用户未登陆，以userKey作为key
        return userInfo.getUserKey();

    }


    //@Scheduled(fixedRate = 5000)
    //@Override
    //public void scheduledTest(){
    //    System.out.println("这是一个定时任务！" + System.currentTimeMillis());
    //}

    @Async
    public ListenableFuture<String> executor1() {
        try {
            System.out.println("executor1方法开始执行");
            TimeUnit.SECONDS.sleep(4);
            System.out.println("executor1方法结束执行。。。");
        } catch (InterruptedException e) {
            System.out.println("executor1异常响应：");
            return AsyncResult.forExecutionException(e);//异常响应
        }
        return AsyncResult.forValue("executor1");
    }

    @Async
    public String executor2() {
        try {
            System.out.println("executor2方法开始执行");
            TimeUnit.SECONDS.sleep(5);
            System.out.println("executor2方法结束执行。。。");
            //int i = 1 / 0; // 制造异常
            System.out.println("executor2线程名：" + Thread.currentThread().getName());
            return "executor2"; // 正常响应
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Cart> queryCarts() {
        //1. 查询未登录的购物车
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String unloginKey = KEY_PREFIX + userInfo.getUserKey();//未登录情况下的外层map的key
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(unloginKey);
        //获取内层map的所有value(cart的json字符串）
        List<Object> unloginCartJsons = hashOps.values();
        //将json字符串转换为Cart集合
        List<Cart> unloginCarts = null;
        if (!CollectionUtils.isEmpty(unloginCartJsons)) {
            //反序列化为List<Cart>集合
            unloginCarts = unloginCartJsons.stream().map(cartJson -> {
                return JSON.parseObject(cartJson.toString(), Cart.class);
            }).collect(Collectors.toList());
        }
        //2 获取登陆状态，未登录直接返回
        if (userInfo.getUserId() == null) {
            return unloginCarts;
        }
        //3 已登录，合并登陆状态的购物车
        String loginKey = KEY_PREFIX + userInfo.getUserId();
        //查询已登录的购物车
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginKey);
        if (!CollectionUtils.isEmpty(unloginCarts)) {
            unloginCarts.forEach(cart -> {
                String skuId = cart.getSkuId().toString();
                // 若登陆状态购物车包含了这条购物车记录，合并数量
                if (loginHashOps.hasKey(skuId)) {
                    String cartJson = loginHashOps.get(skuId).toString();
                    BigDecimal count = cart.getCount();
                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount().add(count));
                    // 更新mysql
                    this.cartAsyncService.updateCartByUserIdAndSkuId(cart);
                } else {
                    // 用户购物车不包含未登录时购物车的商品则，新增到mysql
                    cart.setUserId(userInfo.getUserId().toString());
                    this.cartAsyncService.saveCart(cart);
                }
                //更新redis
                loginHashOps.put(skuId, JSON.toJSONString(cart));
            });
            //  删除未登录的购物车，删除redis及mysql中未登录用户的购物车
            this.redisTemplate.delete(unloginKey);
            this.cartAsyncService.deleteCartsByUserId(userInfo.getUserKey());
        }
        //5 查询登陆状态的购物车并返回
        List<Object> loginCartJsons = loginHashOps.values();
        if (CollectionUtils.isEmpty(loginCartJsons)) {
            return null;
        }
        return loginCartJsons.stream().map(loginCartJson -> {
            return JSON.parseObject(loginCartJson.toString(), Cart.class);
        }).collect(Collectors.toList());
    }

    @Override
    public void updateNum(Cart cart) {

        // 获取外层map的key
        String userId = this.getUserId();
        String key = KEY_PREFIX + userId;

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        // 判断该用户的购物车中是否包含该商品
        if (hashOps.hasKey(cart.getSkuId().toString())) {
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            BigDecimal count = cart.getCount();
            if (count.intValue() > 0){
                cart = JSON.parseObject(cartJson, Cart.class);
                cart.setCount(count);
                this.cartAsyncService.updateCartByUserIdAndSkuId(cart);
                hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            }else {
                this.cartAsyncService.deleteByUserIdAndSkuId(userId, cart.getSkuId());
                hashOps.delete(cart.getSkuId().toString());
            }

        }
    }

    public void deleteCart(Long skuId) {
        // 获取外层map的key
        String userId = this.getUserId();
        String key = KEY_PREFIX + userId;

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        if (hashOps.hasKey(skuId.toString())) {
            this.cartAsyncService.deleteByUserIdAndSkuId(userId, skuId);
            hashOps.delete(skuId.toString());
        }
    }
}
