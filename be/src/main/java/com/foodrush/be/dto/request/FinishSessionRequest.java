package com.foodrush.be.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;

public record FinishSessionRequest(

        @PositiveOrZero(message = "Điểm không được âm")
        @Max(value = 1_000_000, message = "Điểm không hợp lý")
        int score,

        @PositiveOrZero(message = "Số bug đã tiêu diệt không được âm")
        @Max(value = 100_000, message = "Số bug đã tiêu diệt không hợp lý")
        int bugsKilled,

        @PositiveOrZero(message = "Số wave đã qua không được âm")
        @Max(value = 10_000, message = "Số wave đã qua không hợp lý")
        int wavesCompleted) {
}
