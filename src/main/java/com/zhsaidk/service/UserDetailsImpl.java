package com.zhsaidk.service;

import com.zhsaidk.database.entity.Role;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private final Integer id;
    private final String name;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserDetails build(Integer id,
                                    String name,
                                    String username,
                                    String password,
                                    Role role){
        return new UserDetailsImpl(
                id, name, username, password, Set.of(role)
        );
    }
}
