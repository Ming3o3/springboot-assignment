package com.gzist.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gzist.project.common.Result;
import com.gzist.project.entity.Product;
import com.gzist.project.service.IProductService;
import com.gzist.project.utils.UserContext;
import com.gzist.project.vo.request.BatchDeleteRequest;
import com.gzist.project.vo.request.ProductQueryRequest;
import com.gzist.project.vo.request.ProductSaveRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

/**
 * 产品控制器
 * 负责处理产品相关的HTTP请求
 * 遵循RESTful设计，使用VO对象封装参数
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Slf4j
@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private IProductService productService;

    @Autowired
    private UserContext userContext;

    /**
     * 产品列表页面
     * 使用VO对象封装查询参数，符合分层架构规范
     * Spring MVC自动将请求参数绑定到VO对象
     * Service层通过@Cacheable自动使用Redis缓存
     */
    @GetMapping("/list")
    public String listPage(ProductQueryRequest queryRequest, Model model) {
        log.info("访问产品列表页 - 参数: {}", queryRequest);
        
        // 调用Service层方法，自动使用Redis缓存
        // 首次查询：从数据库加载并缓存
        // 后续查询：直接从Redis返回（<5ms）
        IPage<Product> page = productService.getProductPage(queryRequest);
        
        log.info("产品列表查询完成 - 总记录数: {}, 当前页: {}/{}", 
                 page.getTotal(), page.getCurrent(), page.getPages());

        model.addAttribute("page", page);
        model.addAttribute("productName", queryRequest.getProductName());
        model.addAttribute("category", queryRequest.getCategory());
        model.addAttribute("minPrice", queryRequest.getMinPrice());
        model.addAttribute("maxPrice", queryRequest.getMaxPrice());

        return "product/list";
    }

    /**
     * 新增产品页面
     */
    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String addPage() {
        return "product/add";
    }

    /**
     * 编辑产品页面
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String editPage(@PathVariable Long id, Model model) {
        Product product = productService.getById(id);
        if (product == null) {
            return "redirect:/product/list";
        }
        model.addAttribute("product", product);
        return "product/edit";
    }

    /**
     * 查询产品列表（API）
     */
    @GetMapping("/api/list")
    @ResponseBody
    public Result<IPage<Product>> list(@Valid ProductQueryRequest queryRequest) {
        IPage<Product> page = productService.getProductPage(queryRequest);
        return Result.success(page);
    }

    /**
     * 新增产品
     */
    @PostMapping("/api/add")
    @ResponseBody
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<String> add(@Valid @RequestBody ProductSaveRequest saveRequest) {
        // 使用UserContext统一获取当前登录用户ID
        Long userId = userContext.getCurrentUserId();
        
        productService.addProduct(saveRequest, userId);
        return Result.success("产品添加成功");
    }

    /**
     * 更新产品
     */
    @PutMapping("/api/update")
    @ResponseBody
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<String> update(@Valid @RequestBody ProductSaveRequest saveRequest) {
        productService.updateProduct(saveRequest);
        return Result.success("产品更新成功");
    }

    /**
     * 删除产品
     */
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<String> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("产品删除成功");
    }

    /**
     * 批量删除产品
     */
    @DeleteMapping("/api/batch-delete")
    @ResponseBody
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<String> batchDelete(@Valid @RequestBody BatchDeleteRequest deleteRequest) {
        productService.batchDeleteProducts(deleteRequest.getIds());
        return Result.success("批量删除成功");
    }

    /**
     * 查看产品详情
     */
    @GetMapping("/api/detail/{id}")
    @ResponseBody
    public Result<Product> detail(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("产品不存在");
        }
        return Result.success(product);
    }
}
