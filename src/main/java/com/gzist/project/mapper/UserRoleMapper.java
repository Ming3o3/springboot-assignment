package com.gzist.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gzist.project.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联Mapper接口
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
