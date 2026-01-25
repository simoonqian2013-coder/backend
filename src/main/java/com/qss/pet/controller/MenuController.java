package com.qss.pet.controller;

import com.qss.pet.common.ApiResponse;
import com.qss.pet.dto.AssignMenuPermissionRequest;
import com.qss.pet.dto.AssignMenuRoleRequest;
import com.qss.pet.dto.MenuCreateRequest;
import com.qss.pet.dto.MenuUpdateRequest;
import com.qss.pet.dto.MenuView;
import com.qss.pet.entity.SysMenu;
import com.qss.pet.security.SecurityUser;
import com.qss.pet.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MenuController {
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/menus")
    public ApiResponse<List<MenuView>> listMenus() {
        return ApiResponse.ok(menuService.listMenuTree());
    }

    @GetMapping("/api/menus/current")
    public ApiResponse<List<MenuView>> listCurrentUserMenus() {
        List<String> permissions = getCurrentPermissionCodes();
        return ApiResponse.ok(menuService.listMenuTreeByPermissions(permissions));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/menus")
    public ApiResponse<SysMenu> createMenu(@Valid @RequestBody MenuCreateRequest request) {
        return ApiResponse.ok(menuService.createMenu(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/menus/{id}")
    public ApiResponse<SysMenu> updateMenu(@PathVariable("id") Long menuId,
                                           @Valid @RequestBody MenuUpdateRequest request) {
        SysMenu menu = menuService.updateMenu(menuId, request);
        if (menu == null) {
            return ApiResponse.error(404, "Menu not found");
        }
        return ApiResponse.ok(menu);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/menus/{id}")
    public ApiResponse<Void> deleteMenu(@PathVariable("id") Long menuId) {
        if (!menuService.deleteMenu(menuId)) {
            return ApiResponse.error(404, "Menu not found");
        }
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/menus/assign-permissions")
    public ApiResponse<Void> assignPermissions(@Valid @RequestBody AssignMenuPermissionRequest request) {
        menuService.assignPermissions(request.getMenuId(), request.getPermissionIds());
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/menus/assign-roles")
    public ApiResponse<Void> assignRoles(@Valid @RequestBody AssignMenuRoleRequest request) {
        menuService.assignRoles(request.getMenuId(), request.getRoleIds());
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/menus/{id}/permissions")
    public ApiResponse<List<Long>> listMenuPermissionIds(@PathVariable("id") Long menuId) {
        return ApiResponse.ok(menuService.listPermissionIdsByMenuId(menuId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/menus/{id}/permissions/details")
    public ApiResponse<List<com.qss.pet.entity.SysPermission>> listMenuPermissionDetails(@PathVariable("id") Long menuId) {
        return ApiResponse.ok(menuService.listPermissionsByMenuId(menuId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/menus/{id}/roles")
    public ApiResponse<List<Long>> listMenuRoleIds(@PathVariable("id") Long menuId) {
        return ApiResponse.ok(menuService.listRoleIdsByMenuId(menuId));
    }

    private List<String> getCurrentPermissionCodes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser)) {
            return Collections.emptyList();
        }
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        if (user.getPermissions() == null || user.getPermissions().isEmpty()) {
            return Collections.emptyList();
        }
        return user.getPermissions().stream()
                .map(permission -> permission.getCode())
                .collect(Collectors.toList());
    }
}
