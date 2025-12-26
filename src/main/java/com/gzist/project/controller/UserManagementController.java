package com.gzist.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gzist.project.common.Result;
import lombok.extern.slf4j.Slf4j;
import com.gzist.project.dto.UserManageDTO;
import com.gzist.project.entity.Role;
import com.gzist.project.entity.User;
import com.gzist.project.service.IUserService;
import com.gzist.project.vo.request.BatchDeleteRequest;
import com.gzist.project.vo.request.UserQueryRequest;
import com.gzist.project.vo.response.UserDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户管理控制器（仅管理员可访问）
 * 遵循分层架构，使用VO对象封装参数
 *
 * @author GZIST
 * @since 2025-12-25
 */
@Slf4j
@Controller
@RequestMapping("/user")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UserManagementController {

    @Autowired
    private IUserService userService;

    /**
     * 用户管理页面
     * 使用VO对象封装查询参数，符合分层架构规范
     */
    @GetMapping("/manage")
    public String managePage(UserQueryRequest queryRequest, Model model) {
        log.info("访问用户管理页 - 参数: {}", queryRequest);

        IPage<User> page = userService.getUserPage(
                queryRequest.getCurrent(),
                queryRequest.getSize(),
                queryRequest.getUsername(),
                queryRequest.getEmail(),
                queryRequest.getStatus()
        );
        
        log.info("用户列表查询完成 - 总记录数: {}, 当前页: {}/{}",
                page.getTotal(), page.getCurrent(), page.getPages());

        model.addAttribute("page", page);
        model.addAttribute("username", queryRequest.getUsername());
        model.addAttribute("email", queryRequest.getEmail());
        model.addAttribute("status", queryRequest.getStatus());

        return "user/manage";
    }

    /**
     * 新增用户页面
     */
    @GetMapping("/add")
    public String addPage(Model model) {
        List<Role> roles = userService.getAllRoles();
        model.addAttribute("roles", roles);
        return "user/add";
    }

    /**
     * 编辑用户页面
     * 业务逻辑（null判断）已移至Service层
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        User user = userService.getUserForEdit(id);
        if (user == null) {
            return "redirect:/user/manage";
        }

        List<Role> roles = userService.getAllRoles();
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
    public Result<IPage<User>> list(@Valid UserQueryRequest queryRequest) {
        IPage<User> page = userService.getUserPage(
                queryRequest.getCurrent(),
                queryRequest.getSize(),
                queryRequest.getUsername(),
                queryRequest.getEmail(),
                queryRequest.getStatus()
        );
        return Result.success(page);
    }

    /**
     * 新增用户
     */
    @PostMapping("/api/add")
    @ResponseBody
    public Result<String> add(@Valid @RequestBody UserManageDTO userDTO) {
        log.info("新增用户请求 - 用户名: {}", userDTO.getUsername());
        userService.createUser(userDTO);
        log.info("用户添加成功 - 用户名: {}", userDTO.getUsername());
        return Result.success("用户添加成功");
    }

    /**
     * 更新用户
     */
    @PutMapping("/api/update")
    @ResponseBody
    public Result<String> update(@Valid @RequestBody UserManageDTO userDTO) {
        userService.updateUser(userDTO);
        return Result.success("用户更新成功");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除用户请求 - ID: {}", id);
        userService.deleteUser(id);
        log.info("用户删除成功 - ID: {}", id);
        return Result.success("用户删除成功");
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/api/batch-delete")
    @ResponseBody
    public Result<String> batchDelete(@Valid @RequestBody BatchDeleteRequest deleteRequest) {
        userService.batchDeleteUsers(deleteRequest.getIds());
        return Result.success("批量删除成功");
    }

    /**
     * 查看用户详情
     */
    @GetMapping("/api/detail/{id}")
    @ResponseBody
    public Result<UserDetailResponse> detail(@PathVariable Long id) {
        UserDetailResponse response = userService.getUserDetail(id);
        return Result.success(response);
    }

    /**
     * 获取所有角色列表
     */
    @GetMapping("/api/roles")
    @ResponseBody
    public Result<List<Role>> getRoles() {
        List<Role> roles = userService.getAllRoles();
        return Result.success(roles);
    }
}



