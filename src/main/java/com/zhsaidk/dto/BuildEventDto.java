package com.zhsaidk.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.sql.Timestamp;

@Value
public class BuildEventDto {
    @NotNull(message = "Field name must be not null")
    String name;

    @NotNull(message = "Field parameters must be not null")
    JsonNode parameters;

    @NotNull(message = "Field localCreatedAt must be not null")
    Timestamp localCreatedAt;
}
