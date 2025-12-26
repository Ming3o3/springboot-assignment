package com.gzist.project.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gzist.project.entity.OperationLog;
import com.gzist.project.mapper.OperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志AOP切面
 * 拦截Controller层方法，自动记录用户操作日志
 * 过滤不可序列化的Spring框架对象，只记录业务参数
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 定义切点：拦截controller包下的所有方法
     */
    @Pointcut("execution(* com.gzist.project.controller..*.*(..))")
    public void operationLog() {
    }

    /**
     * 后置通知：记录操作日志
     */
    @AfterReturning(pointcut = "operationLog()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        try {
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = "anonymous";
            if (authentication != null && authentication.isAuthenticated()) {
                username = authentication.getName();
            }

            // 创建操作日志对象
            OperationLog operationLog = new OperationLog();
            operationLog.setUsername(username);
            operationLog.setOperation(getOperationType(request.getMethod(), request.getRequestURI()));
            operationLog.setMethod(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
            operationLog.setParams(getSerializableParams(joinPoint.getArgs()));
            operationLog.setIp(getIpAddress(request));

            // 保存日志
            operationLogMapper.insert(operationLog);

            // 控制台输出日志
            log.info("操作日志 - 用户: {}, 操作: {}, 方法: {}, IP: {}",
                    username, operationLog.getOperation(), operationLog.getMethod(), operationLog.getIp());

        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    /**
     * 获取可序列化的参数
     * 过滤Spring框架对象，只保留业务VO对象和基本类型
     * 
     * 过滤的对象类型：
     * 1. MultipartFile - 文件上传对象
     * 2. HttpServletRequest/Response - HTTP请求响应对象
     * 3. Model - Spring MVC视图模型
     * 4. BindingResult - 数据绑定结果
     * 5. WebRequest - Web请求对象
     * 6. HttpSession - 会话对象
     * 
     * @param args 方法参数数组
     * @return JSON格式的参数字符串
     */
    private String getSerializableParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        try {
            List<Object> serializableArgs = new ArrayList<>();
            for (Object arg : args) {
                // 跳过null值
                if (arg == null) {
                    continue;
                }
                
                // 过滤MultipartFile（文件上传）
                if (arg instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile) arg;
                    serializableArgs.add("文件: " + file.getOriginalFilename() + 
                            " (" + file.getSize() + " bytes)");
                }
                // 过滤Servlet相关对象
                else if (arg instanceof ServletRequest || 
                         arg instanceof ServletResponse ||
                         arg instanceof HttpServletRequest || 
                         arg instanceof HttpServletResponse) {
                    // 忽略Request和Response对象
                    continue;
                }
                // 过滤Spring MVC相关对象
                else if (arg instanceof Model || 
                         arg instanceof BindingResult ||
                         arg instanceof WebRequest ||
                         arg instanceof HttpSession) {
                    // 忽略Model、BindingResult、WebRequest、Session对象
                    continue;
                }
                // 过滤Authentication对象
                else if (arg instanceof Authentication) {
                    // 忽略Spring Security认证对象
                    continue;
                }
                // 保留业务对象（VO、DTO、Entity等）
                else {
                    serializableArgs.add(arg);
                }
            }
            return objectMapper.writeValueAsString(serializableArgs);
        } catch (Exception e) {
            log.warn("参数序列化失败: {}", e.getMessage());
            return "[]";
        }
    }

    /**
     * 根据请求方法和URI获取操作类型
     */
    private String getOperationType(String method, String uri) {
        if (uri.contains("/login")) {
            return "登录";
        } else if (uri.contains("/register")) {
            return "注册";
        } else if (uri.contains("/logout")) {
            return "退出";
        } else if ("POST".equals(method)) {
            return "新增";
        } else if ("PUT".equals(method)) {
            return "修改";
        } else if ("DELETE".equals(method)) {
            return "删除";
        } else if ("GET".equals(method)) {
            return "查询";
        }
        return "其他";
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
