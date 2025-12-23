package com.gzist.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gzist.project.entity.User;

/**
 * 用户Service接口
 *
 * @author GZIST
 * @since 2025-12-23
 */
public interface IUserService extends IService<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User getUserByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象
     */
    User getUserByEmail(String email);

    /**
     * 用户注册
     *
     * @param user 用户对象
     * @return 是否成功
     */
    boolean register(User user);

    /**
     * 更新最后登录时间
     *
     * @param userId 用户ID
     */
    void updateLastLoginTime(Long userId);
}
