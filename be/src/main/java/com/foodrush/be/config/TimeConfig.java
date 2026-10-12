package com.foodrush.be.config;

import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    @Bean
    public ZoneId appZoneId(AppProperties properties) {
        return ZoneId.of(properties.getTimezone());
    }
}
