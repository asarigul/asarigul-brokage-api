package com.brokage.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.brokage.api.dto.LoginRequest;
import com.brokage.api.dto.LoginResponse;
import com.brokage.api.service.AuthenticationService;

import jakarta.validation.Valid;

@RestController
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;


    @PostMapping("/api/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) throws Exception {
        return authenticationService.login(request.username(), request.password());
    }
}
