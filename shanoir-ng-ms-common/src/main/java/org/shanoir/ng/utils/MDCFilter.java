package org.shanoir.ng.utils;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@Component
public class MDCFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtauthentication = (JwtAuthenticationToken) authentication;
            String userId = "" + jwtauthentication.getToken().getClaim("userId");
            String userName = "" + jwtauthentication.getToken().getClaim("preferred_username");
            MDC.put("username", userName + "(" + userId + ")");
        } else {
            MDC.put("username", "Admin");
        }
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}