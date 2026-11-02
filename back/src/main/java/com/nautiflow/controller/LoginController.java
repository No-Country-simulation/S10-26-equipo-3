package com.nautiflow.controller;

import com.nautiflow.dto.LoginRequest;
import com.nautiflow.dto.LoginResponse;
import com.nautiflow.service.AdminAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    private final AdminAuthService adminAuthService;

    public LoginController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return adminAuthService.login(request);
    }

}
