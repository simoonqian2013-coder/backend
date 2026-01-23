package com.qss.pet.service;

import com.qss.pet.dto.MenuCreateRequest;
import com.qss.pet.dto.MenuUpdateRequest;
import com.qss.pet.dto.MenuView;
import com.qss.pet.entity.SysMenu;

import java.util.List;

public interface MenuService {
    List<MenuView> listMenuTree();

    List<MenuView> listMenuTreeByRoleIds(List<Long> roleIds);

    SysMenu createMenu(MenuCreateRequest request);

    SysMenu updateMenu(Long menuId, MenuUpdateRequest request);

    boolean deleteMenu(Long menuId);

    void assignPermissions(Long menuId, List<Long> permissionIds);
}
