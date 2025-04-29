package com.zhsaidk.http.controller;

import com.zhsaidk.database.repo.ApiKeyRepository;
import com.zhsaidk.service.ApiKeyService;
import com.zhsaidk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyService apiKeyService;


    @GetMapping
    public String user(Principal principal,
                       Model model){
        model.addAttribute("user", userService.getUser(principal));
        return "user/user";
    }

    @GetMapping("/keys")
    public String getKeyPage(Principal principal,
                             Model model){
        model.addAttribute("keys", apiKeyService.getKeys(principal));
        return "key/keys";
    }

    @GetMapping("/key/create")
    public String getCreateKeyPage(){
        return "key/create";
    }
}
