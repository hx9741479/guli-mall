package com.atguigu.gmall.auth.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public interface AuthService {
    void accredit(String loginName, String password, HttpServletRequest request, HttpServletResponse response);
}
