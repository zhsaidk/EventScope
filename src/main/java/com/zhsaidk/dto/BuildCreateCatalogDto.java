package com.zhsaidk.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class BuildCreateCatalogDto {
    @NotBlank(message = "Field name must be not null")
    String name;
    @NotBlank(message = "Field description must be not null")
    String description;
    @NotNull(message = "Field active must be not null")
    Boolean active;
    @NotNull(message = "Field version must be not null")
    String version;
}