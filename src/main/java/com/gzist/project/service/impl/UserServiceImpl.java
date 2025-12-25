package com.gzist.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gzist.project.dto.UserManageDTO;
import com.gzist.project.entity.Product;
import com.gzist.project.entity.Role;
import com.gzist.project.entity.User;
import com.gzist.project.entity.UserRole;
import com.gzist.project.mapper.ProductMapper;
import com.gzist.project.mapper.RoleMapper;
import com.gzist.project.mapper.UserMapper;
import com.gzist.project.mapper.UserRoleMapper;
import com.gzist.project.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
    private ProductMapper productMapper;

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

    @Override
    public IPage<User> getUserPage(Integer current, Integer size, String username, String email, Integer status) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        if (StringUtils.hasText(email)) {
            wrapper.like(User::getEmail, email);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreatedTime);
        
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(UserManageDTO userDTO) {
        // 检查用户名是否已存在
        if (getUserByUsername(userDTO.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (getUserByEmail(userDTO.getEmail()) != null) {
            throw new RuntimeException("邮箱已存在");
        }

        // 密码不能为空
        if (!StringUtils.hasText(userDTO.getPassword())) {
            throw new RuntimeException("密码不能为空");
        }

        // 创建用户对象
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setRealName(userDTO.getRealName());
        user.setStatus(userDTO.getStatus() != null ? userDTO.getStatus() : 1);

        // 保存用户
        boolean result = this.save(user);

        if (result && userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            // 分配角色
            updateUserRoles(user.getId(), userDTO.getRoleIds());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserManageDTO userDTO) {
        if (userDTO.getId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        User existUser = this.getById(userDTO.getId());
        if (existUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户名是否被其他用户占用
        User userByUsername = getUserByUsername(userDTO.getUsername());
        if (userByUsername != null && !userByUsername.getId().equals(userDTO.getId())) {
            throw new RuntimeException("用户名已被其他用户使用");
        }

        // 检查邮箱是否被其他用户占用
        User userByEmail = getUserByEmail(userDTO.getEmail());
        if (userByEmail != null && !userByEmail.getId().equals(userDTO.getId())) {
            throw new RuntimeException("邮箱已被其他用户使用");
        }

        // 更新用户信息
        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setRealName(userDTO.getRealName());
        user.setStatus(userDTO.getStatus());

        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        boolean result = this.updateById(user);

        // 更新角色
        if (result && userDTO.getRoleIds() != null) {
            updateUserRoles(userDTO.getId(), userDTO.getRoleIds());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        // 将该用户创建的产品的created_by设为NULL（逻辑外键级联处理）
        Product productUpdate = new Product();
        productUpdate.setCreatedBy(null);
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getCreatedBy, userId);
        productMapper.update(productUpdate, productWrapper);
        
        // 删除用户角色关联
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        userRoleMapper.delete(wrapper);

        // 删除用户
        return this.removeById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteUsers(Long[] userIds) {
        // 将这些用户创建的产品的created_by设为NULL（逻辑外键级联处理）
        Product productUpdate = new Product();
        productUpdate.setCreatedBy(null);
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.in(Product::getCreatedBy, Arrays.asList(userIds));
        productMapper.update(productUpdate, productWrapper);
        
        // 删除用户角色关联
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserRole::getUserId, Arrays.asList(userIds));
        userRoleMapper.delete(wrapper);

        // 批量删除用户
        return this.removeByIds(Arrays.asList(userIds));
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        return roleMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserRoles(Long userId, List<Long> roleIds) {
        // 删除原有角色
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        userRoleMapper.delete(wrapper);

        // 添加新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }

        return true;
    }
}
