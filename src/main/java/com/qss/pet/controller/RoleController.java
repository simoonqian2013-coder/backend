package com.qss.pet.controller;

import com.qss.pet.common.ApiResponse;
import com.qss.pet.dto.AssignPermissionRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qss.pet.dto.RoleCreateRequest;
import com.qss.pet.dto.RoleUpdateRequest;
import com.qss.pet.entity.SysRole;
import com.qss.pet.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/roles")
    public ApiResponse<Page<SysRole>> listRoles(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String name) {
        return ApiResponse.ok(roleService.pageRoles(page, size, name));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/roles")
    public ApiResponse<SysRole> createRole(@Valid @RequestBody RoleCreateRequest request) {
        return ApiResponse.ok(roleService.createRole(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/roles/{id}")
    public ApiResponse<SysRole> updateRole(@PathVariable("id") Long roleId,
                                           @Valid @RequestBody RoleUpdateRequest request) {
        SysRole role = roleService.updateRole(roleId, request);
        if (role == null) {
            return ApiResponse.error(404, "Role not found");
        }
        return ApiResponse.ok(role);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/roles/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable("id") Long roleId) {
        if (!roleService.deleteRole(roleId)) {
            return ApiResponse.error(404, "Role not found");
        }
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/roles/assign-permissions")
    public ApiResponse<Void> assignPermissions(@Valid @RequestBody AssignPermissionRequest request) {
        roleService.assignPermissions(request);
        return ApiResponse.ok(null);
    }
}
