package com.mj.aiknowledgebase.service;

import com.mj.aiknowledgebase.domain.po.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author MJ
 * @since 2026-07-04
 */
public interface IUserService extends IService<User> {

    User login(User user);
}
