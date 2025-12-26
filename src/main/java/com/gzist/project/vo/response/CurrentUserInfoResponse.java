package com.gzist.project.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 当前用户信息响应VO
 *
 * @author GZIST
 * @since 2025-12-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 是否已认证
     */
    private Boolean authenticated;

    /**
     * 权限列表
     */
    private List<String> authorities;

    /**
     * Principal信息
     */
    private String principal;
}
