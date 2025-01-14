package com.java2nb.novel.core.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 11797
 */
@Data
@Component
@ConfigurationProperties(prefix="aliyun.oss")
public class OssProperties{

    private String endpoint;

    private String accessKeyId;

    private String accessKeySecret;

    private String region;

    private String bucketName;



}
