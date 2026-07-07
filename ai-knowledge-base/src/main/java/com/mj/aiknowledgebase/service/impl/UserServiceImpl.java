package com.mj.aiknowledgebase.service.impl;

import com.mj.aiknowledgebase.domain.po.User;
import com.mj.aiknowledgebase.mapper.UserMapper;
import com.mj.aiknowledgebase.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author MJ
 * @since 2026-07-04
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public User login(User user) {
        User one = lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .eq(User::getPassword, user.getPassword())
                .one();

        if (one == null){
            throw new RuntimeException("用户不存在");
        }
        if (!one.getPassword().equals(user.getPassword())){
            throw new RuntimeException("密码错误");
        }
        one.setPassword(null);
        return one;
    }

    @Override
    public void register(Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null){
            throw new RuntimeException("用户名或密码不能为空");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        boolean save = save(user);
        if (!save){
            throw new RuntimeException("注册失败");
        }
    }
}
