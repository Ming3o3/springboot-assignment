package com.gzist.project.vo.response;

import com.gzist.project.entity.Role;
import com.gzist.project.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户详情响应VO
 *
 * @author GZIST
 * @since 2025-12-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户信息
     */
    private User user;

    /**
     * 角色列表
     */
    private List<Role> roles;
}
