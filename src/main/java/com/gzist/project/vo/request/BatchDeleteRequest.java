package com.gzist.project.vo.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 批量删除请求VO
 *
 * @author GZIST
 * @since 2025-12-25
 */
@Data
public class BatchDeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID数组
     */
    @NotEmpty(message = "删除ID列表不能为空")
    private Long[] ids;
}
