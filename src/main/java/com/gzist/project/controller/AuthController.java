package com.gzist.project.controller;

import com.gzist.project.common.Result;
import lombok.extern.slf4j.Slf4j;
import com.gzist.project.dto.RegisterDTO;
import com.gzist.project.entity.User;
import com.gzist.project.service.IUserService;
import com.gzist.project.vo.response.CurrentUserInfoResponse;
import org.springframework.beans.BeanUtils;
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
     * 密码一致性校验已移至RegisterDTO的@AssertTrue注解
     * Controller层只负责调用Service，不包含业务验证逻辑
     */
    @PostMapping("/api/register")
    @ResponseBody
    public Result<String> register(@Valid @RequestBody RegisterDTO registerDTO) {
        log.info("用户注册请求 - 用户名: {}, 邮箱: {}", registerDTO.getUsername(), registerDTO.getEmail());

        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        
        userService.register(user);
        log.info("用户注册成功 - 用户名: {}", registerDTO.getUsername());
        return Result.success("注册成功，请登录");
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/api/check-username")
    @ResponseBody
    public Result<Boolean> checkUsername(@RequestParam String username) {
        User user = userService.getUserByUsername(username);
        return Result.success(user == null);
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/api/check-email")
    @ResponseBody
    public Result<Boolean> checkEmail(@RequestParam String email) {
        User user = userService.getUserByEmail(email);
        return Result.success(user == null);
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
