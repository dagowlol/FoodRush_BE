package com.foodrush.be.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;

public record FinishSessionRequest(

        @PositiveOrZero(message = "score must not be negative")
        @Max(value = 1_000_000, message = "score is unrealistically high")
        int score,

        @PositiveOrZero(message = "bugsKilled must not be negative")
        @Max(value = 100_000, message = "bugsKilled is unrealistically high")
        int bugsKilled,

        @PositiveOrZero(message = "wavesCompleted must not be negative")
        @Max(value = 10_000, message = "wavesCompleted is unrealistically high")
        int wavesCompleted) {
}
