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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志AOP切面
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
     * 过滤掉MultipartFile、HttpServletRequest、HttpServletResponse等不可序列化的对象
     */
    private String getSerializableParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        try {
            List<Object> serializableArgs = new ArrayList<>();
            for (Object arg : args) {
                if (arg == null) {
                    serializableArgs.add(null);
                } else if (arg instanceof MultipartFile) {
                    // MultipartFile转换为文件名信息
                    MultipartFile file = (MultipartFile) arg;
                    serializableArgs.add("文件: " + file.getOriginalFilename() + 
                            " (" + file.getSize() + " bytes)");
                } else if (arg instanceof HttpServletRequest || 
                           arg instanceof HttpServletResponse) {
                    // 忽略Request和Response对象
                    continue;
                } else {
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
