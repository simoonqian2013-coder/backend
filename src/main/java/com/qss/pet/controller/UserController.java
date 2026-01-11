package com.qss.pet.controller;

import com.qss.pet.common.ApiResponse;
import com.qss.pet.dto.AssignRoleRequest;
import com.qss.pet.dto.UserCreateRequest;
import com.qss.pet.dto.UserView;
import com.qss.pet.entity.SysUser;
import com.qss.pet.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('sys:user:read')")
    @GetMapping("/api/users")
    public ApiResponse<List<UserView>> listUsers() {
        List<UserView> users = userService.listUsers().stream()
                .map(this::toView)
                .collect(Collectors.toList());
        return ApiResponse.ok(users);
    }

    @PreAuthorize("hasAuthority('sys:user:create')")
    @PostMapping("/api/users")
    public ApiResponse<UserView> createUser(@Valid @RequestBody UserCreateRequest request) {
        SysUser user = userService.createUser(request);
        return ApiResponse.ok(toView(user));
    }

    @PreAuthorize("hasAuthority('sys:user:update')")
    @PostMapping("/api/users/assign-roles")
    public ApiResponse<Void> assignRoles(@Valid @RequestBody AssignRoleRequest request) {
        userService.assignRoles(request.getUserId(), request.getRoleIds());
        return ApiResponse.ok(null);
    }

    private UserView toView(SysUser user) {
        UserView view = new UserView();
        view.setId(user.getId());
        view.setUsername(user.getUsername());
        view.setNickname(user.getNickname());
        view.setEmail(user.getEmail());
        view.setPhone(user.getPhone());
        view.setStatus(user.getStatus());
        view.setLastLoginAt(user.getLastLoginAt());
        view.setCreatedAt(user.getCreatedAt());
        view.setUpdatedAt(user.getUpdatedAt());
        return view;
    }
}
