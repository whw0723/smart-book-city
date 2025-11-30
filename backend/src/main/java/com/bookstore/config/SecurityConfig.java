package com.bookstore.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 安全配置类，提供密码编码器Bean和Web安全配置
 */
@Configuration
@EnableWebSecurity
@EnableAutoConfiguration(exclude = {UserDetailsServiceAutoConfiguration.class})
public class SecurityConfig {

    /**
     * 创建BCrypt密码编码器Bean
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * 配置Web安全，禁用默认的表单登录和基本认证
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护（开发环境）
            .csrf().disable()
            // 允许所有请求访问
            .authorizeRequests()
                .anyRequest().permitAll()
            .and()
            // 禁用表单登录
            .formLogin().disable()
            // 禁用HTTP基本认证
            .httpBasic().disable()
            // 禁用会话管理
            .sessionManagement().disable();
        
        return http.build();
    }
}