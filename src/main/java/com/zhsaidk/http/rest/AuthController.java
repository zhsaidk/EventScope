package com.zhsaidk.http.rest;

import com.zhsaidk.dto.LoginDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

//@RestController
//@RequiredArgsConstructor
//public class AuthController {
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto,
//                                BindingResult bindingResult){
//        if (bindingResult.hasErrors()){
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//        }
//
//    }
//}
