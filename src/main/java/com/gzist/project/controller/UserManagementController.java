package com.gzist.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gzist.project.common.Result;
import com.gzist.project.dto.UserManageDTO;
import com.gzist.project.entity.Role;
import com.gzist.project.entity.User;
import com.gzist.project.mapper.RoleMapper;
import com.gzist.project.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器（仅管理员可访问）
 *
 * @author GZIST
 * @since 2025-12-25
 */
@Controller
@RequestMapping("/user")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UserManagementController {

    @Autowired
    private IUserService userService;

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 用户管理页面
     */
    @GetMapping("/manage")
    public String managePage(Model model,
                             @RequestParam(defaultValue = "1") Integer current,
                             @RequestParam(defaultValue = "10") Integer size,
                             @RequestParam(required = false) String username,
                             @RequestParam(required = false) String email,
                             @RequestParam(required = false) Integer status) {

        IPage<User> page = userService.getUserPage(current, size, username, email, status);

        model.addAttribute("page", page);
        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("status", status);

        return "user/manage";
    }

    /**
     * 新增用户页面
     */
    @GetMapping("/add")
    public String addPage(Model model) {
        // 查询所有角色
        List<Role> roles = roleMapper.selectList(null);
        model.addAttribute("roles", roles);
        return "user/add";
    }

    /**
     * 编辑用户页面
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        User user = userService.getById(id);
        if (user == null) {
            return "redirect:/user/manage";
        }

        // 查询所有角色
        List<Role> roles = roleMapper.selectList(null);
        
        // 查询用户当前角色
        List<Role> userRoles = userService.getUserRoles(id);

        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        model.addAttribute("userRoles", userRoles);

        return "user/edit";
    }

    /**
     * 查询用户列表（API）
     */
    @GetMapping("/api/list")
    @ResponseBody
    public Result<IPage<User>> list(@RequestParam(defaultValue = "1") Integer current,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     @RequestParam(required = false) String username,
                                     @RequestParam(required = false) String email,
                                     @RequestParam(required = false) Integer status) {

        IPage<User> page = userService.getUserPage(current, size, username, email, status);
        return Result.success(page);
    }

    /**
     * 新增用户
     */
    @PostMapping("/api/add")
    @ResponseBody
    public Result<String> add(@Valid @RequestBody UserManageDTO userDTO, BindingResult bindingResult) {
        // 校验参数
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            userService.createUser(userDTO);
            return Result.success("用户添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/api/update")
    @ResponseBody
    public Result<String> update(@Valid @RequestBody UserManageDTO userDTO, BindingResult bindingResult) {
        // 校验参数
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            userService.updateUser(userDTO);
            return Result.success("用户更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return Result.success("用户删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/api/batch-delete")
    @ResponseBody
    public Result<String> batchDelete(@RequestBody Long[] ids) {
        try {
            userService.batchDeleteUsers(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查看用户详情
     */
    @GetMapping("/api/detail/{id}")
    @ResponseBody
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 隐藏密码
        user.setPassword(null);

        // 获取用户角色
        List<Role> roles = userService.getUserRoles(id);

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("roles", roles);

        return Result.success(data);
    }

    /**
     * 获取所有角色列表
     */
    @GetMapping("/api/roles")
    @ResponseBody
    public Result<List<Role>> getAllRoles() {
        List<Role> roles = roleMapper.selectList(null);
        return Result.success(roles);
    }
}
