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
                ur.setUsername(user.getUsername());
                ur.setRoleCode(userRole.getRoleCode());
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

        if (result && userDTO.getRoleCodes() != null && !userDTO.getRoleCodes().isEmpty()) {
            // 分配角色（直接使用roleCodes）
            updateUserRoles(user.getUsername(), userDTO.getRoleCodes());
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

        // 更新角色（直接使用roleCodes）
        if (result && userDTO.getRoleCodes() != null) {
            User updatedUser = this.getById(userDTO.getId());
            updateUserRoles(updatedUser.getUsername(), userDTO.getRoleCodes());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        // 先获取用户信息
        User user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 将该用户创建的产品的created_by_username设为NULL（逻辑外键级联处理）
        Product productUpdate = new Product();
        productUpdate.setCreatedByUsername(null);
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getCreatedByUsername, user.getUsername());
        productMapper.update(productUpdate, productWrapper);
        
        // 删除用户角色关联（使用username）
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUsername, user.getUsername());
        userRoleMapper.delete(wrapper);

        // 删除用户
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteUsers(Long[] ids) {
        // 先获取所有用户的username
        List<User> users = this.listByIds(Arrays.asList(ids));
        if (users == null || users.isEmpty()) {
            return false;
        }
        
        List<String> usernames = new java.util.ArrayList<>();
        for (User user : users) {
            usernames.add(user.getUsername());
        }
        
        // 将这些用户创建的产品的created_by_username设为NULL（逻辑外键级联处理）
        Product productUpdate = new Product();
        productUpdate.setCreatedByUsername(null);
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.in(Product::getCreatedByUsername, usernames);
        productMapper.update(productUpdate, productWrapper);
        
        // 删除用户角色关联（使用username）
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserRole::getUsername, usernames);
        userRoleMapper.delete(wrapper);

        // 批量删除用户
        return this.removeByIds(Arrays.asList(ids));
    }

    @Override
    public List<Role> getUserRoles(String username) {
        return roleMapper.selectByUsername(username);
    }

    @Override
    public com.gzist.project.vo.response.UserDetailResponse getUserDetail(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new com.gzist.project.exception.BusinessException("用户不存在");
        }
        
        // 隐藏密码
        user.setPassword(null);
        
        // 获取用户角色（使用username）
        List<Role> roles = getUserRoles(user.getUsername());
        
        return new com.gzist.project.vo.response.UserDetailResponse(user, roles);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserRoles(String username, List<String> roleCodes) {
        // 删除原有角色（使用username）
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUsername, username);
        userRoleMapper.delete(wrapper);

        // 添加新角色（使用username和role_code）
        if (roleCodes != null && !roleCodes.isEmpty()) {
            for (String roleCode : roleCodes) {
                UserRole userRole = new UserRole();
                userRole.setUsername(username);
                userRole.setRoleCode(roleCode);
                userRoleMapper.insert(userRole);
            }
        }

        return true;
    }

    /**
     * 用户注册（使用DTO）
     * 将DTO到Entity的转换逻辑从Controller移到Service层
     * 
     * @param registerDTO 注册DTO
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean registerFromDTO(com.gzist.project.dto.RegisterDTO registerDTO) {
        User user = new User();
        org.springframework.beans.BeanUtils.copyProperties(registerDTO, user);
        return register(user);
    }

    /**
     * 获取用户用于编辑
     * 业务逻辑：如果用户不存在，返回null（由Controller处理重定向）
     * 
     * @param id 用户ID
     * @return 用户对象，不存在返回null
     */
    @Override
    public User getUserForEdit(Long id) {
        return this.getById(id);
    }

    /**
     * 检查用户名是否可用
     * 业务逻辑：判断用户名是否已存在
     * 
     * @param username 用户名
     * @return true-可用，false-已存在
     */
    @Override
    public boolean isUsernameAvailable(String username) {
        User user = getUserByUsername(username);
        return user == null;
    }

    /**
     * 检查邮箱是否可用
     * 业务逻辑：判断邮箱是否已存在
     * 
     * @param email 邮箱
     * @return true-可用，false-已存在
     */
    @Override
    public boolean isEmailAvailable(String email) {
        User user = getUserByEmail(email);
        return user == null;
    }
}
