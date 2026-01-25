package com.qss.pet.service;

import com.qss.pet.dto.MenuCreateRequest;
import com.qss.pet.dto.MenuUpdateRequest;
import com.qss.pet.dto.MenuView;
import com.qss.pet.entity.SysMenu;
import com.qss.pet.entity.SysPermission;

import java.util.List;

public interface MenuService {
    List<MenuView> listMenuTree();

    List<MenuView> listMenuTreeByRoleIds(List<Long> roleIds);

    List<MenuView> listMenuTreeByPermissions(List<String> permissions);

    SysMenu createMenu(MenuCreateRequest request);

    SysMenu updateMenu(Long menuId, MenuUpdateRequest request);

    boolean deleteMenu(Long menuId);

    void assignPermissions(Long menuId, List<Long> permissionIds);

    void assignRoles(Long menuId, List<Long> roleIds);

    List<Long> listRoleIdsByMenuId(Long menuId);

    List<Long> listPermissionIdsByMenuId(Long menuId);

    List<SysPermission> listPermissionsByMenuId(Long menuId);
}
