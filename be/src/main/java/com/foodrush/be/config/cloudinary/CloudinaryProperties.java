package com.foodrush.be.config.cloudinary;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.cloudinary")
@Getter
@Setter
public class CloudinaryProperties {
    private String cloudName;
    private String apiKey;
    private String apiSecret;
    private String folder = "five-star/vouchers";
    private long maxFileSizeBytes = 5_242_880L;
    private List<String> allowedContentTypes = List.of("image/jpeg", "image/png", "image/webp");
}
