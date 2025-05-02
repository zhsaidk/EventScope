package com.zhsaidk.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class BuildProjectDTO {
    @NotNull(message = "Field name must not be null")
    private String name;

    @NotNull(message = "Field description must not be null")
    private String description;

    @NotNull(message = "Field active must not be null")
    private Boolean active;

}