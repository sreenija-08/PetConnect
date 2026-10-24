package com.petconnect.config;

import com.petconnect.security.UserAccountStatusFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserAccountStatusFilter userAccountStatusFilter;

    @Autowired
    public SecurityConfig(UserAccountStatusFilter userAccountStatusFilter) {
        this.userAccountStatusFilter = userAccountStatusFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            String accept = request.getHeader("Accept");
            String xReq = request.getHeader("X-Requested-With");
            if ((accept != null && accept.contains("application/json")) || "XMLHttpRequest".equalsIgnoreCase(xReq)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Access denied\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/access-denied");
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            if (exception instanceof org.springframework.security.authentication.DisabledException) {
                response.sendRedirect(request.getContextPath() + "/login?disabled=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/login?error=true");
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Public Assets and Pages
                .requestMatchers(
                    "/",
                    "/browse-pets",
                    "/pet-details/**",
                    "/how-it-works",
                    "/contact",
                    "/login",
                    "/register",
                    "/forgot-password",
                    "/reset-password",
                    "/access-denied",
                    "/error",
                    "/error/**",
                    "/css/**",
                    "/js/**",
                    "/uploads/pets/**",
                    "/uploads/shelters/**",
                    "/favicon.ico"
                ).permitAll()
                // Protected Verification Documents Endpoint
                .requestMatchers("/shelters/*/documents/**", "/shelter-documents/**").hasAnyRole("ADMIN", "SHELTER_OWNER")
                // Role Restrictions
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/owner/**", "/shelter/**").hasRole("SHELTER_OWNER")
                .requestMatchers("/user/**", "/pet/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable()) // disable CSRF for local testing simplicity
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler())
            )
            .addFilterBefore(userAccountStatusFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .failureHandler(authenticationFailureHandler())
                .successHandler((request, response, authentication) -> {
                    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                    for (GrantedAuthority authority : authorities) {
                        if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                            response.sendRedirect("/admin/dashboard");
                            return;
                        } else if ("ROLE_SHELTER_OWNER".equals(authority.getAuthority())) {
                            response.sendRedirect("/owner/dashboard");
                            return;
                        } else if ("ROLE_USER".equals(authority.getAuthority())) {
                            response.sendRedirect("/user/dashboard");
                            return;
                        }
                    }
                    response.sendRedirect("/");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }
}
