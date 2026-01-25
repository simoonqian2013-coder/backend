package com.qss.pet.service;

import com.qss.pet.dto.PermissionCreateRequest;
import com.qss.pet.dto.PermissionTreeNode;
import com.qss.pet.dto.PermissionUpdateRequest;
import com.qss.pet.entity.SysPermission;

import java.util.List;

public interface PermissionService {
    SysPermission createPermission(PermissionCreateRequest request);

    SysPermission updatePermission(Long permissionId, PermissionUpdateRequest request);

    boolean deletePermission(Long permissionId);

    List<SysPermission> listPermissions();

    List<PermissionTreeNode> listPermissionTree();
}
