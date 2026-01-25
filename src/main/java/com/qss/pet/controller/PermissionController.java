package com.qss.pet.controller;

import com.qss.pet.common.ApiResponse;
import com.qss.pet.dto.PermissionCreateRequest;
import com.qss.pet.dto.PermissionUpdateRequest;
import com.qss.pet.dto.PermissionTreeNode;
import com.qss.pet.entity.SysPermission;
import com.qss.pet.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    @GetMapping("/api/permissions/tree")
    public ApiResponse<List<PermissionTreeNode>> listPermissionTree() {
        return ApiResponse.ok(permissionService.listPermissionTree());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/permissions")
    public ApiResponse<SysPermission> createPermission(@Valid @RequestBody PermissionCreateRequest request) {
        return ApiResponse.ok(permissionService.createPermission(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/permissions/{id}")
    public ApiResponse<SysPermission> updatePermission(@PathVariable("id") Long permissionId,
                                                       @Valid @RequestBody PermissionUpdateRequest request) {
        SysPermission permission = permissionService.updatePermission(permissionId, request);
        if (permission == null) {
            return ApiResponse.error(404, "Permission not found");
        }
        return ApiResponse.ok(permission);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/permissions/{id}")
    public ApiResponse<Void> deletePermission(@PathVariable("id") Long permissionId) {
        if (!permissionService.deletePermission(permissionId)) {
            return ApiResponse.error(404, "Permission not found");
        }
        return ApiResponse.ok(null);
    }
}
