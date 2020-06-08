package com.atguigu.gmall.ums.service.impl;

import brave.http.HttpServerRequest;
import brave.http.HttpServerResponse;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.ums.api.vo.UserEntity;
import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                queryWrapper.eq("username", data);
                break;
            case 2:
                queryWrapper.eq("phone", data);
                break;
            case 3:
                queryWrapper.eq("email", data);
                break;
            default:
                break;
        }
        //如果数据库中有，就不是0，则表示此用户名数据库没有，此用户名可用
        return this.count(queryWrapper) == 0;
    }

    @Override
    public void register(UserEntity userEntity, String code) {
        //校验短信验证码 TODO

        //生成盐
        String salt = StringUtils.replace(UUID.randomUUID().toString(), "-", "");
        //System.out.println(UUID.randomUUID().toString() + "AAAAAAAAAAAAAAA");
        //对密码加密
        userEntity.setPassword(DigestUtils.md2Hex(salt + DigestUtils.md2Hex(userEntity.getPassword())));

        //设置创建时间
        userEntity.setCreateTime(new Date());
        userEntity.setLevelId(1l);
        userEntity.setStatus(1);
        userEntity.setIntegration(0);
        userEntity.setGrowth(0);
        userEntity.setUsername(userEntity.getUsername());
        userEntity.setNickname(userEntity.getNickname());
        userEntity.setSalt(salt);
        //添加到数据库
        boolean b = this.save(userEntity);
        //注册成功，删除redis中的记录 TODO
    }

    @Override
    public UserEntity queryUser(String loginName, String password) {
        //1 根据登陆名查询用户信息（拿到盐）
        UserEntity userEntity = this.getOne(new QueryWrapper<UserEntity>()
                .eq("username", loginName).or()
                .eq("phone", loginName).or()
                .eq("email", loginName)
        );
        //2 判断用户是否为空
        if (userEntity == null) {
            log.info("账户输入不合法！");
        }
        //3 对密码进行,并和数据库中的密码进行比较
        password = DigestUtils.md5Hex(password + userEntity.getSalt());
        if(!StringUtils.equals(userEntity.getPassword(),password)){
            log.info("密码输入错误！");
        }
        //4 返回用户信息
        return userEntity;
    }
}