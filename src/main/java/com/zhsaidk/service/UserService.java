package com.zhsaidk.service;

import com.zhsaidk.database.entity.ApiKey;
import com.zhsaidk.database.entity.Role;
import com.zhsaidk.database.entity.User;
import com.zhsaidk.database.repo.UserRepository;
import com.zhsaidk.dto.UserReadDto;
import com.zhsaidk.mapper.UserReadMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserReadMapper readMapper;

    public User getUser(Principal principal){
        return userRepository.findUserByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public List<ApiKey> getKeys(Principal principal){
        User user = userRepository.findUserByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return user.getKeys();
    }

    public List<UserReadDto> findAllUsersAsDto(){
        return userRepository.findAll()
                .stream().map(readMapper::map)
                .toList();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User currentUser = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return new org.springframework.security.core.userdetails.User(
                currentUser.getUsername(),
                currentUser.getPassword(),
                Set.of(currentUser.getRole())
        );
    }
}
