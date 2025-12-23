package com.gzist.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Controller
public class IndexController {

    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/product/list";
    }

    /**
     * 主页
     */
    @GetMapping("/index")
    public String home() {
        return "index";
    }
}
