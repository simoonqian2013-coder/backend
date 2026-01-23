package com.qss.pet.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AssignMenuPermissionRequest {
    @NotNull
    private Long menuId;

    private List<Long> permissionIds;

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
