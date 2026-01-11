package com.qss.pet.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qss.pet.dto.AssignPermissionRequest;
import com.qss.pet.dto.RoleCreateRequest;
import com.qss.pet.entity.SysRole;
import com.qss.pet.mapper.SysRoleMapper;
import com.qss.pet.mapper.SysRolePermissionMapper;
import com.qss.pet.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    public RoleServiceImpl(SysRoleMapper roleMapper, SysRolePermissionMapper rolePermissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    public SysRole createRole(RoleCreateRequest request) {
        SysRole role = new SysRole();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(role);
        return role;
    }

    @Override
    public List<SysRole> listRoles() {
        return roleMapper.selectList(Wrappers.lambdaQuery());
    }

    @Override
    @Transactional
    public void assignPermissions(AssignPermissionRequest request) {
        rolePermissionMapper.deletePermissionsByRoleId(request.getRoleId());
        for (Long permissionId : request.getPermissionIds()) {
            rolePermissionMapper.insertRolePermission(request.getRoleId(), permissionId);
        }
    }
}
