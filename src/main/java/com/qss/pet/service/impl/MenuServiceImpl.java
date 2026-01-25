package com.qss.pet.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qss.pet.dto.MenuCreateRequest;
import com.qss.pet.dto.MenuPermissionDetail;
import com.qss.pet.dto.MenuPermissionView;
import com.qss.pet.dto.MenuUpdateRequest;
import com.qss.pet.dto.MenuView;
import com.qss.pet.entity.SysMenu;
import com.qss.pet.entity.SysPermission;
import com.qss.pet.mapper.SysMenuMapper;
import com.qss.pet.mapper.SysMenuPermissionMapper;
import com.qss.pet.mapper.SysPermissionMapper;
import com.qss.pet.mapper.SysRoleMenuMapper;
import com.qss.pet.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuPermissionMapper menuPermissionMapper;
    private final SysPermissionMapper permissionMapper;

    public MenuServiceImpl(SysMenuMapper menuMapper,
                           SysRoleMenuMapper roleMenuMapper,
                           SysMenuPermissionMapper menuPermissionMapper,
                           SysPermissionMapper permissionMapper) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuPermissionMapper = menuPermissionMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public List<MenuView> listMenuTree() {
        List<SysMenu> menus = menuMapper.selectList(Wrappers.lambdaQuery(SysMenu.class)
                .orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId));
        return buildTree(menus);
    }

    @Override
    public List<MenuView> listMenuTreeByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleIds(roleIds);
        if (menuIds == null || menuIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<SysMenu> menus = menuMapper.selectList(Wrappers.lambdaQuery(SysMenu.class)
                .in(SysMenu::getId, menuIds)
                .orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId));
        List<Long> parentIds = menus.stream()
                .map(SysMenu::getParentId)
                .filter(parentId -> parentId != null && parentId != 0L)
                .distinct()
                .filter(parentId -> menus.stream().noneMatch(m -> Objects.equals(m.getId(), parentId)))
                .collect(Collectors.toList());
        if (!parentIds.isEmpty()) {
            List<SysMenu> parents = menuMapper.selectList(Wrappers.lambdaQuery(SysMenu.class)
                    .in(SysMenu::getId, parentIds));
            menus.addAll(parents);
        }
        return buildTree(menus);
    }

    @Override
    public List<MenuView> listMenuTreeByPermissions(List<String> permissions) {
        List<MenuView> menuTree = listMenuTree();
        if (permissions == null) {
            permissions = Collections.emptyList();
        }
        return filterMenuByPermissions(menuTree, permissions);
    }

    @Override
    @Transactional
    public SysMenu createMenu(MenuCreateRequest request) {
        validateParent(request.getParentId(), null);
        SysMenu menu = new SysMenu();
        menu.setParentId(normalizeParentId(request.getParentId()));
        menu.setTitle(request.getTitle());
        menu.setPath(request.getPath());
        menu.setIcon(request.getIcon());
        menu.setHeader(request.getHeader());
        menu.setSort(request.getSort());
        menu.setStatus(request.getStatus());
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        menuMapper.insert(menu);
        ensureMenuPermission(menu, null);
        return menu;
    }

    @Override
    @Transactional
    public SysMenu updateMenu(Long menuId, MenuUpdateRequest request) {
        SysMenu menu = menuMapper.selectById(menuId);
        if (menu == null) {
            return null;
        }
        String previousPath = menu.getPath();
        validateParent(request.getParentId(), menuId);
        menu.setParentId(normalizeParentId(request.getParentId()));
        menu.setTitle(request.getTitle());
        menu.setPath(request.getPath());
        menu.setIcon(request.getIcon());
        menu.setHeader(request.getHeader());
        menu.setSort(request.getSort());
        menu.setStatus(request.getStatus());
        menu.setUpdatedAt(LocalDateTime.now());
        menuMapper.updateById(menu);
        ensureMenuPermission(menu, previousPath);
        return menu;
    }

    @Override
    @Transactional
    public boolean deleteMenu(Long menuId) {
        SysMenu menu = menuMapper.selectById(menuId);
        if (menu == null) {
            return false;
        }
        List<SysMenu> children = menuMapper.selectList(Wrappers.lambdaQuery(SysMenu.class)
                .eq(SysMenu::getParentId, menuId));
        for (SysMenu child : children) {
            deleteMenuRelations(child.getId());
            menuMapper.deleteById(child.getId());
        }
        deleteMenuRelations(menuId);
        menuMapper.deleteById(menuId);
        return true;
    }

    @Override
    @Transactional
    public void assignPermissions(Long menuId, List<Long> permissionIds) {
        menuPermissionMapper.deletePermissionsByMenuId(menuId);
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        for (Long permissionId : permissionIds) {
            menuPermissionMapper.insertMenuPermission(menuId, permissionId);
        }
    }

    @Override
    @Transactional
    public void assignRoles(Long menuId, List<Long> roleIds) {
        roleMenuMapper.deleteRolesByMenuId(menuId);
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            roleMenuMapper.insertRoleMenu(roleId, menuId);
        }
    }

    @Override
    public List<Long> listRoleIdsByMenuId(Long menuId) {
        return roleMenuMapper.selectRoleIdsByMenuId(menuId);
    }

    private void deleteMenuRelations(Long menuId) {
        roleMenuMapper.deleteRolesByMenuId(menuId);
        menuPermissionMapper.deletePermissionsByMenuId(menuId);
    }

    private void validateParent(Long parentId, Long menuId) {
        Long normalizedParentId = normalizeParentId(parentId);
        if (normalizedParentId == null) {
            return;
        }
        if (menuId != null && Objects.equals(menuId, normalizedParentId)) {
            throw new IllegalArgumentException("Parent menu cannot be itself");
        }
        SysMenu parent = menuMapper.selectById(normalizedParentId);
        if (parent == null) {
            throw new IllegalArgumentException("Parent menu not found");
        }
        if (parent.getParentId() != null && parent.getParentId() != 0L) {
            throw new IllegalArgumentException("Menu supports at most 2 levels");
        }
    }

    private Long normalizeParentId(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return null;
        }
        return parentId;
    }

    private List<MenuView> buildTree(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> menuIds = menus.stream()
                .map(SysMenu::getId)
                .collect(Collectors.toList());
        Map<Long, List<String>> permissionMap = buildPermissionMap(menuIds);
        Map<Long, List<MenuView>> childrenMap = new HashMap<>();
        List<MenuView> roots = new ArrayList<>();
        for (SysMenu menu : menus) {
            MenuView view = toView(menu, permissionMap.get(menu.getId()));
            Long parentId = menu.getParentId();
            if (parentId == null || parentId == 0L) {
                roots.add(view);
            } else {
                childrenMap.computeIfAbsent(parentId, key -> new ArrayList<>()).add(view);
            }
        }
        for (MenuView root : roots) {
            List<MenuView> children = childrenMap.get(root.getId());
            if (children != null) {
                children.sort(Comparator.comparing(MenuView::getSort, Comparator.nullsLast(Integer::compareTo)));
                root.setChildren(children);
            }
        }
        roots.sort(Comparator.comparing(MenuView::getSort, Comparator.nullsLast(Integer::compareTo)));
        return roots;
    }

    private Map<Long, List<String>> buildPermissionMap(List<Long> menuIds) {
        List<MenuPermissionView> rows = menuPermissionMapper.selectPermissionCodesByMenuIds(menuIds);
        if (rows == null || rows.isEmpty()) {
            return new HashMap<>();
        }
        Map<Long, List<String>> permissionMap = new HashMap<>();
        for (MenuPermissionView row : rows) {
            if (row.getCode() != null && row.getCode().startsWith("menu:")) {
                permissionMap.computeIfAbsent(row.getMenuId(), key -> new ArrayList<>()).add(row.getCode());
            }
        }
        return permissionMap;
    }

    private MenuView toView(SysMenu menu, List<String> permissions) {
        MenuView view = new MenuView();
        view.setId(menu.getId());
        view.setParentId(menu.getParentId());
        view.setTitle(menu.getTitle());
        view.setPath(menu.getPath());
        view.setIcon(menu.getIcon());
        view.setHeader(menu.getHeader());
        view.setSort(menu.getSort());
        view.setStatus(menu.getStatus());
        if (permissions != null && !permissions.isEmpty()) {
            view.setPermissions(permissions);
            view.setAuth(permissions);
        } else {
            view.setPermissions(Collections.emptyList());
            view.setAuth(Collections.emptyList());
        }
        return view;
    }

    private List<MenuView> filterMenuByPermissions(List<MenuView> menus, List<String> permissions) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }
        List<MenuView> result = new ArrayList<>();
        for (MenuView menu : menus) {
            List<MenuView> children = filterMenuByPermissions(menu.getChildren(), permissions);
            boolean hasPermission = menu.getAuth() == null || menu.getAuth().isEmpty()
                    || permissions.stream().anyMatch(menu.getAuth()::contains);
            if (hasPermission || (children != null && !children.isEmpty())) {
                MenuView copy = new MenuView();
                copy.setId(menu.getId());
                copy.setParentId(menu.getParentId());
                copy.setTitle(menu.getTitle());
                copy.setPath(menu.getPath());
                copy.setIcon(menu.getIcon());
                copy.setHeader(menu.getHeader());
                copy.setSort(menu.getSort());
                copy.setStatus(menu.getStatus());
                copy.setPermissions(menu.getPermissions());
                copy.setAuth(menu.getAuth());
                copy.setChildren(children);
                result.add(copy);
            }
        }
        return result;
    }

    private void ensureMenuPermission(SysMenu menu, String previousPath) {
        if (menu == null) {
            return;
        }
        String newCode = menuPermissionCode(menu.getPath());
        if (newCode.isEmpty()) {
            return;
        }
        SysPermission permission = null;
        String previousCode = menuPermissionCode(previousPath);
        if (!previousCode.isEmpty() && !previousCode.equals(newCode)) {
            permission = permissionMapper.selectOne(Wrappers.lambdaQuery(SysPermission.class)
                    .eq(SysPermission::getCode, previousCode));
            if (permission != null) {
                permission.setCode(newCode);
            }
        }
        if (permission == null) {
            permission = permissionMapper.selectOne(Wrappers.lambdaQuery(SysPermission.class)
                    .eq(SysPermission::getCode, newCode));
        }
        if (permission == null) {
            permission = new SysPermission();
            permission.setCode(newCode);
            permission.setName(menu.getTitle());
            permission.setType("menu");
            permission.setPath(menu.getPath());
            permission.setSort(menu.getSort());
            permission.setStatus(menu.getStatus());
            permission.setCreatedAt(LocalDateTime.now());
            permission.setUpdatedAt(LocalDateTime.now());
            permissionMapper.insert(permission);
        } else {
            permission.setCode(newCode);
            permission.setName(menu.getTitle());
            permission.setType("menu");
            permission.setPath(menu.getPath());
            permission.setSort(menu.getSort());
            permission.setStatus(menu.getStatus());
            permission.setUpdatedAt(LocalDateTime.now());
            permissionMapper.updateById(permission);
        }
        ensureMenuPermissionMapping(menu.getId(), permission.getId());
    }

    private void ensureMenuPermissionMapping(Long menuId, Long permissionId) {
        if (menuId == null || permissionId == null) {
            return;
        }
        List<MenuPermissionDetail> details = menuPermissionMapper
                .selectPermissionDetailsByMenuIds(Collections.singletonList(menuId));
        if (details == null) {
            details = Collections.emptyList();
        }
        Set<Long> existingIds = new HashSet<>();
        for (MenuPermissionDetail detail : details) {
            existingIds.add(detail.getId());
        }
        if (!existingIds.contains(permissionId)) {
            menuPermissionMapper.insertMenuPermission(menuId, permissionId);
        }
    }

    private String menuPermissionCode(String path) {
        if (path == null) {
            return "";
        }
        String trimmed = path.startsWith("/") ? path.substring(1) : path;
        if (trimmed.isEmpty()) {
            return "";
        }
        return "menu:" + trimmed.replace("/", ":");
    }
}
