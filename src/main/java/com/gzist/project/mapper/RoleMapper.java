package com.gzist.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gzist.project.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper接口
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户名查询角色列表
     *
     * @param username 用户名
     * @return 角色列表
     */
    @Select("SELECT r.* FROM roles r " +
            "INNER JOIN user_roles ur ON r.role_code = ur.role_code " +
            "WHERE ur.username = #{username}")
    List<Role> selectByUsername(@Param("username") String username);

    /**
     * 根据角色代码查询角色
     *
     * @param roleCode 角色代码
     * @return 角色对象
     */
    @Select("SELECT * FROM roles WHERE role_code = #{roleCode}")
    Role selectByRoleCode(@Param("roleCode") String roleCode);
}
