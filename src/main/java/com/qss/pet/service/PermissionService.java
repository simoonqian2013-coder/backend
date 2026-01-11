package com.qss.pet.service;

import com.qss.pet.dto.PermissionCreateRequest;
import com.qss.pet.entity.SysPermission;

import java.util.List;

public interface PermissionService {
    SysPermission createPermission(PermissionCreateRequest request);

    List<SysPermission> listPermissions();
}
