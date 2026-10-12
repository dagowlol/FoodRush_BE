package com.foodrush.be.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String timezone = "Asia/Ho_Chi_Minh";
    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Coupon coupon = new Coupon();
    private Seed seed = new Seed();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long expirationMs = 86_400_000L;
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Coupon {
        private int expiryDays = 7;
    }

    @Getter
    @Setter
    public static class Seed {
        private boolean enabled = true;
        private String staffUsername = "staff";
        private String staffEmail = "staff@fivestar.local";
        private String staffPassword;
        private String staffDisplayName = "Five Star Staff";
        private String staffPasswordFile = "staff-credentials.txt";
    }
}
