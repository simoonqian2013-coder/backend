package com.qss.pet.controller;

import com.qss.pet.common.ApiResponse;
import com.qss.pet.dto.AssignPermissionRequest;
import com.qss.pet.dto.RoleCreateRequest;
import com.qss.pet.entity.SysRole;
import com.qss.pet.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/roles")
    public ApiResponse<List<SysRole>> listRoles() {
        return ApiResponse.ok(roleService.listRoles());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/roles")
    public ApiResponse<SysRole> createRole(@Valid @RequestBody RoleCreateRequest request) {
        return ApiResponse.ok(roleService.createRole(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/roles/assign-permissions")
    public ApiResponse<Void> assignPermissions(@Valid @RequestBody AssignPermissionRequest request) {
        roleService.assignPermissions(request);
        return ApiResponse.ok(null);
    }
}
