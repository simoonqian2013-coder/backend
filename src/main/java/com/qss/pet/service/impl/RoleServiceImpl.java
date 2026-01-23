package com.qss.pet.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qss.pet.dto.AssignPermissionRequest;
import com.qss.pet.dto.RoleCreateRequest;
import com.qss.pet.dto.RoleUpdateRequest;
import com.qss.pet.entity.SysRole;
import com.qss.pet.mapper.SysRoleMapper;
import com.qss.pet.mapper.SysRoleMenuMapper;
import com.qss.pet.mapper.SysRolePermissionMapper;
import com.qss.pet.mapper.SysUserRoleMapper;
import com.qss.pet.service.RoleService;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public RoleServiceImpl(SysRoleMapper roleMapper,
                           SysRolePermissionMapper rolePermissionMapper,
                           SysUserRoleMapper userRoleMapper,
                           SysRoleMenuMapper roleMenuMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
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
    public Page<SysRole> pageRoles(int page, int size, String name) {
        Page<SysRole> rolePage = new Page<>(page, size);
        return roleMapper.selectPage(
                rolePage,
                Wrappers.lambdaQuery(SysRole.class)
                        .like(StringUtils.hasText(name), SysRole::getName, name)
                        .orderByDesc(SysRole::getCreatedAt)
        );
    }

    @Override
    public SysRole updateRole(Long roleId, RoleUpdateRequest request) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            return null;
        }
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.updateById(role);
        return role;
    }

    @Override
    @Transactional
    public boolean deleteRole(Long roleId) {
        rolePermissionMapper.deletePermissionsByRoleId(roleId);
        userRoleMapper.deleteUsersByRoleId(roleId);
        return roleMapper.deleteById(roleId) > 0;
    }

    @Override
    @Transactional
    public void assignPermissions(AssignPermissionRequest request) {
        rolePermissionMapper.deletePermissionsByRoleId(request.getRoleId());
        for (Long permissionId : request.getPermissionIds()) {
            rolePermissionMapper.insertRolePermission(request.getRoleId(), permissionId);
        }
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.deleteMenusByRoleId(roleId);
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Long menuId : menuIds) {
            roleMenuMapper.insertRoleMenu(roleId, menuId);
        }
    }
}
