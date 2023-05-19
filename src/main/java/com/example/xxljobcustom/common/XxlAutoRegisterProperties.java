package com.example.xxljobcustom.common;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//@ConfigurationProperties(prefix = "admin")
@Data
@Component
public class XxlAutoRegisterProperties {
    @Value("${xxl.job.admin.addresses}")
    private String url;
    @Value("${xxl.job.admin.username}")
    private String username;
    @Value("${xxl.job.admin.password}")
    private String password;
    @Value("${xxl.job.executor.appName}")
    private String appName;
    @Value("${xxl.job.executor.title}")
    private String title;
}
//@Component
