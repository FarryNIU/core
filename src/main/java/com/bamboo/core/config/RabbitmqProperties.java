package com.bamboo.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author FarryNiu 2025/12/8
 */
@Component
@ConfigurationProperties(prefix = "bamboo.rabbitmq")
@Data
public class RabbitmqProperties {
    private String host;
    private int port;
    private String virtualHost;
    private String username;
    private String password;
}
