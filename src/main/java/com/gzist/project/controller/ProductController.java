package com.gzist.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gzist.project.common.Result;
import com.gzist.project.entity.Product;
import com.gzist.project.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
     */
    @GetMapping("/list")
    public String listPage(Model model,
                           @RequestParam(defaultValue = "1") Integer current,
                           @RequestParam(defaultValue = "10") Integer size,
                           @RequestParam(required = false) String productName,
                           @RequestParam(required = false) String category,
                           @RequestParam(required = false) BigDecimal minPrice,
                           @RequestParam(required = false) BigDecimal maxPrice) {

        IPage<Product> page = productService.getProductPage(current, size, productName, category, minPrice, maxPrice);

        model.addAttribute("page", page);
        model.addAttribute("productName", productName);
        model.addAttribute("category", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

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
    public Result<IPage<Product>> list(@RequestParam(defaultValue = "1") Integer current,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String productName,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) BigDecimal minPrice,
                                        @RequestParam(required = false) BigDecimal maxPrice) {

        IPage<Product> page = productService.getProductPage(current, size, productName, category, minPrice, maxPrice);
        return Result.success(page);
    }

    /**
     * 新增产品
     */
    @PostMapping("/api/add")
    @ResponseBody
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<String> add(@RequestBody Product product) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 这里简化处理，实际应该从UserService中获取用户ID
            // 暂时设置为null，实际应该改为获取用户ID
            productService.addProduct(product, 1L);

            return Result.success("产品添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新产品
     */
    @PutMapping("/api/update")
    @ResponseBody
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<String> update(@RequestBody Product product) {
        try {
            productService.updateProduct(product);
            return Result.success("产品更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除产品
     */
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<String> delete(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return Result.success("产品删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除产品
     */
    @DeleteMapping("/api/batch-delete")
    @ResponseBody
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<String> batchDelete(@RequestBody Long[] ids) {
        try {
            productService.batchDeleteProducts(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
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

    /**
     * 测试管理员权限（调试用）
     */
    @GetMapping("/api/test-admin")
    @ResponseBody
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<String> testAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Result.success("管理员权限验证成功！用户：" + authentication.getName() + 
                             "，权限：" + authentication.getAuthorities());
    }
}
