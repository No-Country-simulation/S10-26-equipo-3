package com.nautiflow.controller;

import com.nautiflow.dto.AdminDashboardDTO;
import com.nautiflow.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminDashboardController {
    private final AdminService adminService;

    public AdminDashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    @Operation(tags = "Dashboard")
    public AdminDashboardDTO dashboard() {
        return adminService.obtenerDashboard();
    }
}
