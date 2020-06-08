package com.atguigu.gmall.auth.service.impl;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.UmsClient;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.api.vo.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@EnableConfigurationProperties(JwtProperties.class)
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UmsClient umsClient;

    @Override
    public void accredit(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        try {
            //1 获取用户信息
            ResponseVo<UserEntity> userEntityResponseVo = this.umsClient.queryUser(loginName, password);
            UserEntity userEntity = userEntityResponseVo.getData();
            //2 判断用户信息是否为空
            if(userEntity == null){
                log.error("用户名或者密码有误！");
            }
            //3 把用户id及用户名放入载荷
            Map<String, Object> map = new HashMap<>();
            map.put("userId",userEntity.getId());
            map.put("username",userEntity.getUsername());
            //4 为了防止jwt被别人盗取，载荷中加入用户ip地址
            String ipAddressAtService = IpUtil.getIpAddressAtService(request);
            map.put("ip",ipAddressAtService);
            //5 制作jwt类型的token信息
            String token = JwtUtils.generateToken(map, this.jwtProperties.getPrivateKey(), this.jwtProperties.getExpire());
            //6 把jwt放入cookie中
            CookieUtils.setCookie(request, response, this.jwtProperties.getCookieName(), token, this.jwtProperties.getExpire() * 60);
            //7 用户昵称放入cookie中，方便页面展示昵称
            CookieUtils.setCookie(request,response,this.jwtProperties.getUnick(),userEntity.getNickname(),this.jwtProperties.getExpire() * 60);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("用户名或密码出错！");
        }
    }

}
