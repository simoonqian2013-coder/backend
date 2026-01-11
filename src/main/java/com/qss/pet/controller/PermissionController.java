package com.qss.pet.controller;

import com.qss.pet.common.ApiResponse;
import com.qss.pet.dto.PermissionCreateRequest;
import com.qss.pet.entity.SysPermission;
import com.qss.pet.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/permissions")
    public ApiResponse<List<SysPermission>> listPermissions() {
        return ApiResponse.ok(permissionService.listPermissions());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/permissions")
    public ApiResponse<SysPermission> createPermission(@Valid @RequestBody PermissionCreateRequest request) {
        return ApiResponse.ok(permissionService.createPermission(request));
    }
}
