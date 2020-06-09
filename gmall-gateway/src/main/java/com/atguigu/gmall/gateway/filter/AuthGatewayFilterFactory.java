package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@EnableConfigurationProperties({JwtProperties.class})
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig> {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 一定要重写构造方法
     * 告诉父类，这里使用PathConfig对象接收配置内容
     */
    public AuthGatewayFilterFactory() {
        super(PathConfig.class);
    }

    @Override
    public GatewayFilter apply(PathConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                //获取request和response，注意：不是HttpServletRequest及HttpServletResponse
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();
                //获取当前请求的path路径
                String path = request.getURI().toString();

                //1. 判断请求路径在不在拦截名单中，不在直接放行
                Boolean flag = false;
                for (String authPath : config.getAuthPaths()) {
                    //如果白名单中有一个包含当前路径
                    if (path.indexOf(authPath) != -1) {
                        //true为包含
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    //放行
                    return chain.filter(exchange);
                }
                //2. 获取请求中的token
                String token = "";
                //异步请求，通过头信息获取token
                List<String> tokenList = request.getHeaders().get("token");
                if (!CollectionUtils.isEmpty(tokenList)) {
                    token = tokenList.get(0);
                } else {
                    //同步请求通过cokie
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                    if (CollectionUtils.isEmpty(cookies) || !cookies.containsKey(jwtProperties.getCookieName())) {
                        // cookie为空直接拦截，重定向到登陆
                        //状态码表示由于请求对应的资源存在着另一个URI，应重定向获取请求的资源
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + path);
                        //设置响应状态码为未认证
                        //response.setStatusCode(HttpStatus.UNAUTHORIZED);
                        return response.setComplete();
                    }
                    //获取cookie中的jwt 每个网站下面（Cookies)有第一个cookies （看了一下是访问域名地址）
                    token = cookies.getFirst(jwtProperties.getCookieName()).getValue();
                }
                //判断token是否为空
                if(StringUtils.isBlank(token)){
                    //去登陆
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + path);
                    //设置响应状态码为未认证
                    //response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }
                try {
                    //3. 获取token解析jwt获取登陆信息
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
                    //4. 判断是否被盗用，通过登陆信息中的ip和当前请求的ip比较
                    String ip = map.get("ip").toString();
                    String curIp = IpUtil.getIpAddressAtGateway(request);
                    if(!StringUtils.equals(ip,curIp)){
                        //去登陆
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + path);
                        //设置响应状态码为未认证
                        //response.setStatusCode(HttpStatus.UNAUTHORIZED);
                        return response.setComplete();
                    }
                    //5. 传递登陆信息给后续服务，后续服务就不用再去解析了
                    //将userId转变成request对象。
                    String userId = map.get("userId").toString();
                    request.mutate().header("userId",userId).build();
                    exchange.mutate().request(request).build();
                    //6. 放行
                    return chain.filter(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                    //去登陆
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + path);
                    //设置响应状态码为未认证
                    //response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }
            }
        };
    }

    /**
     * 指定字段顺序
     * 可以通过不通的字段分别读取：/to'Login.html,/login
     * 在这里希望通过一个集合字段读取所有的路径
     *
     * @return
     */
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("authPaths");
    }

    /**
     * 指定读取结果集字段的类型
     *
     * @return
     */
    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    /**
     * 读取配置类的内部类
     */
    @Data
    public static class PathConfig {
        private List<String> authPaths;
    }

}
