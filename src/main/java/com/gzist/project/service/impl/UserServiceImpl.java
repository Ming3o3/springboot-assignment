package com.gzist.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gzist.project.entity.Role;
import com.gzist.project.entity.User;
import com.gzist.project.entity.UserRole;
import com.gzist.project.mapper.RoleMapper;
import com.gzist.project.mapper.UserMapper;
import com.gzist.project.mapper.UserRoleMapper;
import com.gzist.project.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户Service实现类
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User getUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(User user) {
        // 检查用户名是否已存在
        if (getUserByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (getUserByEmail(user.getEmail()) != null) {
            throw new RuntimeException("邮箱已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认状态
        user.setStatus(1);

        // 保存用户
        boolean result = this.save(user);

        if (result) {
            // 分配默认角色（ROLE_USER）
            Role userRole = roleMapper.selectByRoleCode("ROLE_USER");
            if (userRole != null) {
                UserRole ur = new UserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(userRole.getId());
                userRoleMapper.insert(ur);
            }
        }

        return result;
    }

    @Override
    public void updateLastLoginTime(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);
    }
}
