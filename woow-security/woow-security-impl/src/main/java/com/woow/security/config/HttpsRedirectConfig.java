package com.woow.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@Configuration
public class HttpsRedirectConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
        return server -> server.addContextCustomizers((Context context) -> context.setUseHttpOnly(true));
    }

    @Bean
    public WebMvcConfigurer httpsEnforcer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
                        String proto = request.getHeader("x-forwarded-proto");
                        if ("http".equalsIgnoreCase(proto)) {
                            String redirectUrl = "https://" + request.getServerName() + request.getRequestURI();
                            if (request.getQueryString() != null) {
                                redirectUrl += "?" + request.getQueryString();
                            }
                            response.sendRedirect(redirectUrl);
                            return false;
                        }
                        return true;
                    }
                });
            }
        };
    }
}
