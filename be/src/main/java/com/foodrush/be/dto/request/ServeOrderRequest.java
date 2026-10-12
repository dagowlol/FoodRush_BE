package com.foodrush.be.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ServeOrderRequest(

        @NotEmpty(message = "servedItemIds must not be empty")
        List<Long> servedItemIds) {
}
