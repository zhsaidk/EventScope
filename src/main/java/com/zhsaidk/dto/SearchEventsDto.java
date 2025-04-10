package com.zhsaidk.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Value
public class SearchEventsDto {
    String name;
    Timestamp begin;
    Timestamp end;
}
