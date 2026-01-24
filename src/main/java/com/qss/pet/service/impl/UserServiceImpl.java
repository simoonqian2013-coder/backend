package com.qss.pet.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qss.pet.dto.UserCreateRequest;
import com.qss.pet.dto.UserUpdateRequest;
import com.qss.pet.entity.SysPermission;
import com.qss.pet.entity.SysRole;
import com.qss.pet.entity.SysUser;
import com.qss.pet.mapper.SysPermissionMapper;
import com.qss.pet.mapper.SysRoleMapper;
import com.qss.pet.mapper.SysUserMapper;
import com.qss.pet.mapper.SysUserRoleMapper;
import com.qss.pet.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(SysUserMapper userMapper,
                           SysUserRoleMapper userRoleMapper,
                           SysRoleMapper roleMapper,
                           SysPermissionMapper permissionMapper,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SysUser getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    @Transactional
    public SysUser createUser(UserCreateRequest request) {
        SysUser existing = userMapper.selectByUsername(request.getUsername());
        if (existing != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), request.getRoleIds());
        }

        return user;
    }

    @Override
    public List<SysUser> listUsers() {
        return userMapper.selectList(Wrappers.lambdaQuery());
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteRolesByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            userRoleMapper.insertUserRole(userId, roleId);
        }
    }

    @Override
    public List<SysRole> listRolesByUserId(Long userId) {
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        return roles == null ? Collections.emptyList() : roles;
    }

    @Override
    public List<SysPermission> listPermissionsByUserId(Long userId) {
        List<SysPermission> perms = permissionMapper.selectPermissionsByUserId(userId);
        return perms == null ? Collections.emptyList() : perms;
    }

    @Override
    public void updateLastLogin(Long userId) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        SysUser existing = userMapper.selectById(userId);
        if (existing == null) {
            return false;
        }
        userRoleMapper.deleteRolesByUserId(userId);
        userMapper.deleteById(userId);
        return true;
    }

    @Override
    @Transactional
    public SysUser updateUser(Long userId, UserUpdateRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        user.setStatus(request.getStatus());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }
}
