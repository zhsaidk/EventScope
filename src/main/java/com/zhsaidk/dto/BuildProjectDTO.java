package com.zhsaidk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

@Value
public class BuildProjectDTO {
    @NotBlank(message = "Field name must not be null")
    String name;

    @Length(min = 2, max = 64)
    @NotBlank(message = "Field description must not be null")
    String description;

    @NotNull(message = "Field active must not be null")
    Boolean active;
}