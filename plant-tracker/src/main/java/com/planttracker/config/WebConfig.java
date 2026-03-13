package com.planttracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        // use exact origins (no trailing slash) or patterns:
                        .allowedOriginPatterns(
                            "https://*.netlify.app",           // allow netlify subdomains
                            "http://localhost:5173",          // local dev
                            "https://csce548.onrender.com",   // your Render domain (no trailing slash)
                            "https://69a26a241c8f10f797f0d67a--funny-liger-c00f87.netlify.app" // if needed, exact preview host
                        )
                        .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}