package com.gzist.project.config;

import com.gzist.project.entity.Role;
import com.gzist.project.entity.User;
import com.gzist.project.mapper.RoleMapper;
import com.gzist.project.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security配置类
 *
 * @author GZIST
 * @since 2025-12-23
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 用户详情服务
     */
    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return username -> {
            // 根据用户名查询用户
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("用户不存在");
            }

            // 查询用户角色
            List<Role> roles = roleMapper.selectByUserId(user.getId());
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role.getRoleCode()))
                    .collect(Collectors.toList());

            // 返回UserDetails对象
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(user.getStatus() == 0)
                    .credentialsExpired(false)
                    .disabled(user.getStatus() == 0)
                    .build();
        };
    }

    /**
     * Remember-Me持久化Token仓库
     */
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        // 首次启动时自动创建表，后续可注释掉
//         tokenRepository.setCreateTableOnStartup(true);
        return tokenRepository;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 授权配置
                .authorizeRequests()
                // 允许访问静态资源
                .antMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                // 允许访问注册相关接口
                .antMatchers("/register", "/api/register", "/api/check-username", "/api/check-email", "/api/current-user-info").permitAll()
                // 允许访问登录页面
                .antMatchers("/login").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
                .and()
                // 表单登录配置
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/api/login")
                .defaultSuccessUrl("/product/list", true)
                .failureUrl("/login?error=true")
                .permitAll()
                .and()
                // 退出登录配置
                .logout()
                .logoutUrl("/api/logout")
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true)
                .permitAll()
                .and()
                // Remember-Me配置（记住我，有效期1天）
                .rememberMe()
                .tokenRepository(persistentTokenRepository())
                .tokenValiditySeconds(86400) // 1天
                .userDetailsService(userDetailsService())
                .key("product-management-system")
                .and()
                // Session管理
                .sessionManagement()
                .maximumSessions(1) // 同一用户最多一个session
                .expiredUrl("/login?expired=true")
                .and()
                .and()
                // 暂时禁用CSRF保护（开发环境），生产环境建议启用并在前端添加CSRF token
                .csrf().disable();
    }
}
