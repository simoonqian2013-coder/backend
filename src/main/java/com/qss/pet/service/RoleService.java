package com.qss.pet.service;

import com.qss.pet.dto.AssignPermissionRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qss.pet.dto.RoleCreateRequest;
import com.qss.pet.dto.RoleUpdateRequest;
import com.qss.pet.entity.SysRole;

import java.util.List;

public interface RoleService {
    SysRole createRole(RoleCreateRequest request);

    Page<SysRole> pageRoles(int page, int size, String name);

    List<SysRole> listAllRoles();

    SysRole updateRole(Long roleId, RoleUpdateRequest request);

    boolean deleteRole(Long roleId);

    void assignPermissions(AssignPermissionRequest request);

    void assignMenus(Long roleId, List<Long> menuIds);

    List<Long> listPermissionIdsByRoleId(Long roleId);
}
