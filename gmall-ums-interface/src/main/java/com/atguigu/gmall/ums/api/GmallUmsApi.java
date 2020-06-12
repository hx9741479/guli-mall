package com.atguigu.gmall.ums.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.ums.api.vo.UserAddressEntity;
import com.atguigu.gmall.ums.api.vo.UserEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GmallUmsApi {

    @GetMapping("ums/user/query")
    ResponseVo<UserEntity> queryUser(
            @RequestParam("loginName") String loginName,
            @RequestParam("password") String password);

    /**
     * 根据用户id查询收货地址
     *
     * @param userId
     * @return
     */
    @GetMapping("ums/useraddress/user/{userId}")
    ResponseVo<List<UserAddressEntity>> queryAddressesByUserId(@PathVariable("userId") Long userId);

    /**
     * 根据用户id查询用户信息的方法（用户信息中包含积分信息）
     *
     * @param id
     * @return
     */
    @GetMapping("{id}")
    ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id);


}
