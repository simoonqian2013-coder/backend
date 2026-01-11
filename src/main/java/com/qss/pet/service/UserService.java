package com.qss.pet.service;

import com.qss.pet.dto.UserCreateRequest;
import com.qss.pet.entity.SysPermission;
import com.qss.pet.entity.SysRole;
import com.qss.pet.entity.SysUser;

import java.util.List;

public interface UserService {
    SysUser getByUsername(String username);

    SysUser createUser(UserCreateRequest request);

    List<SysUser> listUsers();

    void assignRoles(Long userId, List<Long> roleIds);

    List<SysRole> listRolesByUserId(Long userId);

    List<SysPermission> listPermissionsByUserId(Long userId);

    void updateLastLogin(Long userId);
}
