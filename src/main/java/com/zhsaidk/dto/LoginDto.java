package com.zhsaidk.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class LoginDto {
    @NotBlank(message = "Имя пользователя не должен быть null")
    String username;
    @NotBlank(message = "Пароль не должен быть null")
    String password;
}
