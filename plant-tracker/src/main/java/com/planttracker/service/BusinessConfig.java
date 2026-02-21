package com.planttracker.service;

import com.planttracker.business.BusinessManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusinessConfig {
    @Bean
    public BusinessManager businessManager() {
        return new BusinessManager();
    }
}