package com.qss.pet.controller;

import com.qss.pet.common.ApiResponse;
import com.qss.pet.dto.LoginRequest;
import com.qss.pet.dto.LoginResponse;
import com.qss.pet.security.JwtService;
import com.qss.pet.security.SecurityUser;
import com.qss.pet.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/api/auth/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

        String token = jwtService.generateToken(securityUser.getUsername());
        userService.updateLastLogin(securityUser.getUser().getId());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtService.getExpirationSeconds());
        response.setUsername(securityUser.getUsername());
        response.setRoles(securityUser.getRoles().stream()
                .map(r -> r.getCode())
                .collect(Collectors.toList()));
        response.setPermissions(securityUser.getPermissions().stream()
                .map(p -> p.getCode())
                .collect(Collectors.toList()));

        return ApiResponse.ok(response);
    }
}
