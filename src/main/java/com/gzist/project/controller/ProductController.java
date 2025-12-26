package com.gzist.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gzist.project.common.Result;
import com.gzist.project.entity.Product;
import com.gzist.project.service.IProductService;
import com.gzist.project.vo.request.BatchDeleteRequest;
import com.gzist.project.vo.request.ProductQueryRequest;
import com.gzist.project.vo.request.ProductSaveRequest;
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
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private IProductService productService;

    /**
     * 产品列表页面
     * 使用VO对象封装查询参数，符合分层架构规范
     * Spring MVC自动将请求参数绑定到VO对象
     */
    @GetMapping("/list")
    public String listPage(ProductQueryRequest queryRequest, Model model) {
        // 使用Service层缓存的方法查询
        IPage<Product> page = productService.getProductPage(queryRequest);

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
    public Result<String> add(@Valid @RequestBody ProductSaveRequest saveRequest, 
                               Authentication authentication) {
        // 获取当前登录用户（这里简化处理，实际应从用户服务获取ID）
        // TODO: 从用户服务根据username获取userId
        Long userId = 1L;
        
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
