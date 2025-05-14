package com.zhsaidk.dto;

import com.zhsaidk.database.entity.PermissionRole;
import com.zhsaidk.database.entity.Role;
import lombok.Value;

@Value
public class UserReadDto {
    Integer id;
    String name;
    String username;
    Role role;
}
