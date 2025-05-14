package com.zhsaidk.mapper;

import com.zhsaidk.database.entity.User;
import com.zhsaidk.dto.UserReadDto;
import org.springframework.stereotype.Component;

@Component
public class UserReadMapper implements Mapper<User, UserReadDto> {
    @Override
    public UserReadDto map(User user) {
        return new UserReadDto(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getRole()
        );
    }
}
