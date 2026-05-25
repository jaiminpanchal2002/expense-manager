package com.expense_manager.expense_manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http

            // Authorization Rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )

            // Login Configuration
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )

            // Logout Configuration
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )

            // Security Headers
            .headers(headers -> headers

                // Content Security Policy (CSP)
                .contentSecurityPolicy(csp ->
                    csp.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';")
                )

                // Prevent Clickjacking
                .frameOptions(frame -> frame.sameOrigin())

                // Referrer Policy
                .referrerPolicy(referrer ->
                    referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
                )
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}