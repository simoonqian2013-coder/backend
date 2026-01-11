package com.qss.pet.service;

import com.qss.pet.dto.AssignPermissionRequest;
import com.qss.pet.dto.RoleCreateRequest;
import com.qss.pet.entity.SysRole;

import java.util.List;

public interface RoleService {
    SysRole createRole(RoleCreateRequest request);

    List<SysRole> listRoles();

    void assignPermissions(AssignPermissionRequest request);
}
