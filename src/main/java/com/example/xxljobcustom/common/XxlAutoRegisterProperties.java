package com.example.xxljobcustom.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin")
@Data
public class XxlAutoRegisterProperties {
    private String url;
    private String username;
    private String password;
    private String appName;
    private String title;
}
//@Component
