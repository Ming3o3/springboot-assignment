package com.gzist.project.controller;

import com.gzist.project.common.Result;
import com.gzist.project.dto.RegisterDTO;
import com.gzist.project.entity.User;
import com.gzist.project.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 认证控制器（登录、注册）
 *
 * @author GZIST
 * @since 2025-12-23
 */
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
    public Result<String> register(@Valid @RequestBody RegisterDTO registerDTO, BindingResult bindingResult) {
        // 校验参数
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage());
        }

        // 校验两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return Result.error("两次密码输入不一致");
        }

        try {
            // 创建用户对象
            User user = new User();
            BeanUtils.copyProperties(registerDTO, user);

            // 注册用户
            userService.register(user);

            return Result.success("注册成功，请登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
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
    public Result<Map<String, Object>> getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> info = new HashMap<>();
        
        if (authentication != null) {
            info.put("username", authentication.getName());
            info.put("authenticated", authentication.isAuthenticated());
            
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            info.put("authorities", authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
            
            info.put("principal", authentication.getPrincipal().toString());
        }
        
        return Result.success(info);
    }
}
