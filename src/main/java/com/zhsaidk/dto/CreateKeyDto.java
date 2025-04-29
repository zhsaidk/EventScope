package com.zhsaidk.dto;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class CreateKeyDto {
    String description;
    Integer hours;
}
