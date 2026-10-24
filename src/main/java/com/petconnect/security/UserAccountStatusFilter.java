package com.petconnect.security;

import com.petconnect.entity.User;
import com.petconnect.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserAccountStatusFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Autowired
    public UserAccountStatusFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String path = request.getRequestURI();
            // Skip static resources
            if (!path.startsWith("/css/") && !path.startsWith("/js/") &&
                !path.startsWith("/images/") && !path.startsWith("/uploads/") &&
                !path.equals("/favicon.ico")) {

                String email = auth.getName();
                if (email != null && !email.isBlank()) {
                    User user = userRepository.findByEmail(email).orElse(null);
                    if (user != null && !user.isActive()) {
                        SecurityContextHolder.clearContext();
                        HttpSession session = request.getSession(false);
                        if (session != null) {
                            session.invalidate();
                        }

                        String accept = request.getHeader("Accept");
                        String xReq = request.getHeader("X-Requested-With");
                        if ((accept != null && accept.contains("application/json")) || "XMLHttpRequest".equalsIgnoreCase(xReq)) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Account deactivated\", \"message\": \"Your account has been deactivated. Please contact the administrator.\"}");
                            return;
                        } else {
                            response.sendRedirect(request.getContextPath() + "/login?disabled=true");
                            return;
                        }
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
