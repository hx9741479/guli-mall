package com.atguigu.gmall.order.interceptor;

import com.atguigu.gmall.order.bean.UserInfo;
import com.atguigu.gmall.order.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor implements HandlerInterceptor {


    //声明线程的局部变量 局部变量内存的内容是UserInfo
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //到这里，肯定已经登陆了，未登录，网关会拦截，让用户去登陆,且登陆信息网关已经解析完成
        UserInfo userInfo = new UserInfo();
        //  获取请求头信息
        Long userId = Long.valueOf(request.getHeader("userId"));
        userInfo.setUserId(userId);
        //传递给后续业务
        THREAD_LOCAL.set(userInfo);
        return true;
    }

    /**
     * 封装了一个获取线程局部变量值的静态方法
     *
     * @return
     */
    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }

    /**
     * 在试图渲染完成之后执行，经常在完成方法中释放资源
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 调用删除方法，是必须选项。因为使用的是tomcat线程池，请求结束后，线程不会结束。
        // 如果不手动删除线程变量，可能会导致内存泄漏
        THREAD_LOCAL.remove();
    }
}
