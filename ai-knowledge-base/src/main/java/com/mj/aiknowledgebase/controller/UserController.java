package com.mj.aiknowledgebase.controller;


import com.mj.aiknowledgebase.common.Result;
import com.mj.aiknowledgebase.domain.po.User;
import com.mj.aiknowledgebase.service.IUserService;
import com.mj.aiknowledgebase.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author MJ
 * @since 2026-07-04
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        log.info("用户登录：{}", user);

        User user1 = userService.login(user);

//        登录成功之后生成令牌
        String token = jwtUtil.generateToken(user1.getUsername(), user1.getId());
        HashMap<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user1);
        return Result.success(data);
    }
}
