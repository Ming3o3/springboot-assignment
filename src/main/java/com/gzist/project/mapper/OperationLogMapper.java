package com.gzist.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gzist.project.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper接口
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
