package org.mpm.server.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

@WebFilter
@Slf4j
public class AuthFilter implements Filter {

    @Autowired
    public UserService userService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String uri = req.getRequestURI();
        if (uri.startsWith("/api") && !uri.startsWith("/api/checkPassword")) {
            if (!userService.checkSignature(req.getHeader("Account"), "", req.getHeader("Signature"))) {
                // 没有登陆的情况下不允许访问任何api
                throw new RuntimeException("签名不对，不允许访问 API");
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void destroy() {

    }
}
