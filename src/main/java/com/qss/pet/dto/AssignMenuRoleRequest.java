package com.qss.pet.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AssignMenuRoleRequest {
    @NotNull
    private Long menuId;

    private List<Long> roleIds;

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
