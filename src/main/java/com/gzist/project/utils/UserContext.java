package com.gzist.project.utils;

import com.gzist.project.entity.User;
import com.gzist.project.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 用户上下文工具类
 * 统一管理当前登录用户信息获取逻辑
 * 避免Controller层直接处理Authentication对象
 *
 * @author GZIST
 * @since 2025-12-26
 */
@Component
public class UserContext {

    @Autowired
    private IUserService userService;

    /**
     * 获取当前登录用户的用户名
     *
     * @return 用户名，未登录返回null
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * 获取当前登录用户的ID
     *
     * @return 用户ID，未登录抛出异常
     * @throws IllegalStateException 用户未登录或用户不存在
     */
    public Long getCurrentUserId() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new IllegalStateException("用户未登录");
        }

        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new IllegalStateException("当前登录用户不存在: " + username);
        }

        return user.getId();
    }

    /**
     * 获取当前登录用户完整信息
     *
     * @return User对象，未登录返回null
     */
    public User getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }
        return userService.getUserByUsername(username);
    }

    /**
     * 判断当前用户是否已登录
     *
     * @return true已登录，false未登录
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * 判断当前用户是否拥有指定角色
     *
     * @param role 角色名称（如"ROLE_ADMIN"）
     * @return true拥有该角色，false不拥有
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
