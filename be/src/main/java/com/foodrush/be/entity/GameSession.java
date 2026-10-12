package com.foodrush.be.entity;

import com.foodrush.be.entity.enums.FoodSafetyStatus;
import com.foodrush.be.entity.enums.SessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
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
        name = "game_sessions",
        indexes = {
                @Index(name = "idx_game_sessions_user", columnList = "user_id"),
                @Index(name = "idx_game_sessions_user_status", columnList = "user_id, status")
        })
public class GameSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_game_sessions_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    @Builder.Default
    @Column(name = "score_earned", nullable = false)
    private int scoreEarned = 0;

    @Builder.Default
    @Column(name = "bugs_killed", nullable = false, columnDefinition = "int not null default 0")
    private int bugsKilled = 0;

    @Builder.Default
    @Column(name = "waves_completed", nullable = false, columnDefinition = "int not null default 0")
    private int wavesCompleted = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "food_safety_status", nullable = false, length = 20,
            columnDefinition = "varchar(20) not null default 'SAFE'")
    private FoodSafetyStatus foodSafetyStatus = FoodSafetyStatus.SAFE;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Version
    @Column(nullable = false)
    private Long version;
}
