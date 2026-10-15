package com.xq.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "xq.mail")
@Data
public class MailProperties {

    private String username;
    private String password;

}
