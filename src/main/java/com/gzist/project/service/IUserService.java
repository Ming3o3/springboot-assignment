package com.gzist.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gzist.project.dto.UserManageDTO;
import com.gzist.project.entity.Role;
import com.gzist.project.entity.User;

import java.util.List;

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

    /**
     * 分页查询用户列表（管理员功能）
     *
     * @param current 当前页
     * @param size 每页大小
     * @param username 用户名（可选）
     * @param email 邮箱（可选）
     * @param status 状态（可选）
     * @return 用户分页数据
     */
    IPage<User> getUserPage(Integer current, Integer size, String username, String email, Integer status);

    /**
     * 创建用户（管理员功能）
     *
     * @param userDTO 用户DTO
     * @return 是否成功
     */
    boolean createUser(UserManageDTO userDTO);

    /**
     * 更新用户信息（管理员功能）
     *
     * @param userDTO 用户DTO
     * @return 是否成功
     */
    boolean updateUser(UserManageDTO userDTO);

    /**
     * 删除用户（管理员功能）
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long userId);

    /**
     * 批量删除用户（管理员功能）
     *
     * @param userIds 用户ID数组
     * @return 是否成功
     */
    boolean batchDeleteUsers(Long[] userIds);

    /**
     * 获取用户的角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getUserRoles(Long userId);

    /**
     * 更新用户角色（管理员功能）
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    boolean updateUserRoles(Long userId, List<Long> roleIds);
}
