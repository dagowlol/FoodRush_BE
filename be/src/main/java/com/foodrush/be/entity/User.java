package com.foodrush.be.entity;

import com.foodrush.be.entity.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        })
public class User extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Builder.Default
    @Column(name = "total_score", nullable = false)
    private long totalScore = 0L;

    @Builder.Default
    @Column(name = "coin_balance", nullable = false)
    private long coinBalance = 0L;

    /**
     * Tài khoản còn được phép đăng nhập hay không. Admin có thể khoá tài khoản
     * từ trang quản trị; tài khoản bị khoá sẽ bị chặn cả khi đăng nhập mới lẫn
     * khi dùng JWT đã cấp trước đó.
     */
    @Builder.Default
    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("true")
    private boolean active = true;

    @Version
    @Column(nullable = false)
    private Long version;
}
