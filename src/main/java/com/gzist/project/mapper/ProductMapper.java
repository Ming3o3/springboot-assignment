package com.gzist.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gzist.project.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品Mapper接口
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
