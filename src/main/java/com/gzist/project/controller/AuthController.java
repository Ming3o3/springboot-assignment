package com.gzist.project.controller;

import com.gzist.project.common.Result;
import lombok.extern.slf4j.Slf4j;
import com.gzist.project.dto.RegisterDTO;
import com.gzist.project.entity.User;
import com.gzist.project.service.IUserService;
import com.gzist.project.vo.response.CurrentUserInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

/**
 * 认证控制器（登录、注册）
 * 专注于认证相关的HTTP请求处理
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Slf4j
@Controller
public class AuthController {

    @Autowired
    private IUserService userService;

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * 用户注册
     */
    @PostMapping("/api/register")
    @ResponseBody
    public Result<String> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.registerFromDTO(registerDTO);
        return Result.success("注册成功，请登录");
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/api/check-username")
    @ResponseBody
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean available = userService.isUsernameAvailable(username);
        return Result.success(available);
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/api/check-email")
    @ResponseBody
    public Result<Boolean> checkEmail(@RequestParam String email) {
        boolean available = userService.isEmailAvailable(email);
        return Result.success(available);
    }

    /**
     * 获取当前用户信息和权限（用于调试）
     */
    @GetMapping("/api/current-user-info")
    @ResponseBody
    public Result<CurrentUserInfoResponse> getCurrentUserInfo(Authentication authentication) {
        if (authentication == null) {
            return Result.error("未登录");
        }

        CurrentUserInfoResponse response = new CurrentUserInfoResponse(
                authentication.getName(),
                authentication.isAuthenticated(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()),
                authentication.getPrincipal().toString()
        );

        return Result.success(response);
    }
}
