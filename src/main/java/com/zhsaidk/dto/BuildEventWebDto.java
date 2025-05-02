package com.zhsaidk.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class BuildEventWebDto {
    @NotNull(message = "Field name must be not null")
    String name;

    @NotNull(message = "Field parameters must be not null")
    String parameters;

    @NotNull(message = "Field localCreatedAt must be not null")
    LocalDateTime localCreatedAt;
}
