package com.qss.pet.dto;

import java.util.List;

public class PermissionTreeNode {
    private Object id;
    private String title;
    private Long menuId;
    private boolean expand;
    private boolean disabled;
    private List<PermissionTreeNode> children;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public boolean isExpand() {
        return expand;
    }

    public void setExpand(boolean expand) {
        this.expand = expand;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public List<PermissionTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<PermissionTreeNode> children) {
        this.children = children;
    }
}
