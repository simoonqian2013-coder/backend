package com.qss.pet.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qss.pet.dto.PermissionCreateRequest;
import com.qss.pet.entity.SysPermission;
import com.qss.pet.mapper.SysPermissionMapper;
import com.qss.pet.service.PermissionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final SysPermissionMapper permissionMapper;

    public PermissionServiceImpl(SysPermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @Override
    public SysPermission createPermission(PermissionCreateRequest request) {
        SysPermission permission = new SysPermission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setType(request.getType());
        permission.setMethod(request.getMethod());
        permission.setPath(request.getPath());
        permission.setParentId(request.getParentId());
        permission.setSort(request.getSort());
        permission.setStatus(request.getStatus());
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        permissionMapper.insert(permission);
        return permission;
    }

    @Override
    public List<SysPermission> listPermissions() {
        return permissionMapper.selectList(Wrappers.lambdaQuery());
    }
}
