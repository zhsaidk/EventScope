package com.zhsaidk.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class BuildProjectDTO {
    @NotNull(message = "Field name must be not null")
    String name;
    @NotNull(message = "Field description must be not null")
    String description;
    @NotNull(message = "Field active must be not null")
    Boolean active;
}
