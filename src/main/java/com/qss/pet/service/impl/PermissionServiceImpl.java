package com.qss.pet.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qss.pet.dto.MenuPermissionDetail;
import com.qss.pet.dto.PermissionCreateRequest;
import com.qss.pet.dto.PermissionTreeNode;
import com.qss.pet.dto.PermissionUpdateRequest;
import com.qss.pet.entity.SysPermission;
import com.qss.pet.entity.SysMenu;
import com.qss.pet.mapper.SysMenuPermissionMapper;
import com.qss.pet.mapper.SysPermissionMapper;
import com.qss.pet.mapper.SysMenuMapper;
import com.qss.pet.mapper.SysRolePermissionMapper;
import com.qss.pet.service.PermissionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final SysPermissionMapper permissionMapper;
    private final SysMenuMapper menuMapper;
    private final SysMenuPermissionMapper menuPermissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    public PermissionServiceImpl(SysPermissionMapper permissionMapper,
                                 SysMenuMapper menuMapper,
                                 SysMenuPermissionMapper menuPermissionMapper,
                                 SysRolePermissionMapper rolePermissionMapper) {
        this.permissionMapper = permissionMapper;
        this.menuMapper = menuMapper;
        this.menuPermissionMapper = menuPermissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    public SysPermission createPermission(PermissionCreateRequest request) {
        SysPermission existing = permissionMapper.selectOne(Wrappers.lambdaQuery(SysPermission.class)
                .eq(SysPermission::getCode, request.getCode()));
        if (existing != null) {
            return existing;
        }
        SysPermission permission = new SysPermission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setType(request.getType());
        permission.setMethod(request.getMethod());
        permission.setPath(request.getPath());
        permission.setParentId(request.getParentId());
        permission.setSort(request.getSort());
        permission.setStatus(request.getStatus());
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        permissionMapper.insert(permission);
        return permission;
    }

    @Override
    public SysPermission updatePermission(Long permissionId, PermissionUpdateRequest request) {
        if (permissionId == null) {
            return null;
        }
        SysPermission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            return null;
        }
        SysPermission duplicate = permissionMapper.selectOne(Wrappers.lambdaQuery(SysPermission.class)
                .eq(SysPermission::getCode, request.getCode())
                .ne(SysPermission::getId, permissionId));
        if (duplicate != null) {
            throw new IllegalArgumentException("Permission code already exists");
        }
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setType(request.getType());
        permission.setMethod(request.getMethod());
        permission.setPath(request.getPath());
        permission.setParentId(request.getParentId());
        permission.setSort(request.getSort());
        permission.setStatus(request.getStatus());
        permission.setUpdatedAt(LocalDateTime.now());
        permissionMapper.updateById(permission);
        return permission;
    }

    @Override
    public boolean deletePermission(Long permissionId) {
        if (permissionId == null) {
            return false;
        }
        SysPermission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            return false;
        }
        menuPermissionMapper.deleteMenusByPermissionId(permissionId);
        rolePermissionMapper.deleteRolesByPermissionId(permissionId);
        permissionMapper.deleteById(permissionId);
        return true;
    }

    @Override
    public List<SysPermission> listPermissions() {
        return permissionMapper.selectList(Wrappers.lambdaQuery());
    }

    @Override
    public List<PermissionTreeNode> listPermissionTree() {
        List<SysMenu> menus = menuMapper.selectList(Wrappers.lambdaQuery(SysMenu.class)
                .orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId));
        if (menus.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> menuIds = menus.stream().map(SysMenu::getId).collect(Collectors.toList());
        List<MenuPermissionDetail> menuPermissionDetails =
                menuPermissionMapper.selectPermissionDetailsByMenuIds(menuIds);
        Map<Long, List<MenuPermissionDetail>> menuPermissionMap = new HashMap<>();
        for (MenuPermissionDetail detail : menuPermissionDetails) {
            menuPermissionMap.computeIfAbsent(detail.getMenuId(), key -> new ArrayList<>()).add(detail);
        }
        List<SysPermission> allPermissions = permissionMapper.selectList(Wrappers.lambdaQuery());
        Map<String, SysPermission> permissionByCode = allPermissions.stream()
                .filter(p -> p.getCode() != null)
                .collect(Collectors.toMap(SysPermission::getCode, p -> p, (a, b) -> a));

        Map<Long, List<SysMenu>> childrenMap = new HashMap<>();
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu menu : menus) {
            Long parentId = menu.getParentId();
            if (parentId == null || parentId == 0L) {
                roots.add(menu);
            } else {
                childrenMap.computeIfAbsent(parentId, key -> new ArrayList<>()).add(menu);
            }
        }

        List<PermissionTreeNode> tree = new ArrayList<>();
        for (SysMenu root : roots) {
            PermissionTreeNode node = buildMenuNode(root, childrenMap, permissionByCode, menuPermissionMap);
            tree.add(node);
        }

        List<MenuPermissionDetail> orphanActions = collectOrphanActions(menuPermissionDetails, permissionByCode);
        if (!orphanActions.isEmpty()) {
            PermissionTreeNode orphanRoot = new PermissionTreeNode();
            orphanRoot.setId("orphan");
            orphanRoot.setTitle("未归类权限");
            orphanRoot.setExpand(true);
            orphanRoot.setChildren(orphanActions.stream()
                    .map(this::toPermissionNode)
                    .collect(Collectors.toList()));
            tree.add(orphanRoot);
        }
        return tree;
    }

    private PermissionTreeNode buildMenuNode(SysMenu menu,
                                             Map<Long, List<SysMenu>> childrenMap,
                                             Map<String, SysPermission> permissionByCode,
                                             Map<Long, List<MenuPermissionDetail>> menuPermissionMap) {
        PermissionTreeNode node = new PermissionTreeNode();
        node.setMenuId(menu.getId());
        SysPermission menuPermission = permissionByCode.get(menuPermissionCode(menu.getPath()));
        if (menuPermission != null) {
            node.setId(menuPermission.getId());
            node.setTitle(menuPermission.getName() + " (" + menuPermission.getCode() + ")");
        } else {
            node.setId("menu-" + menu.getId());
            node.setTitle(menu.getTitle());
            node.setDisabled(true);
        }
        node.setExpand(true);

        List<PermissionTreeNode> children = new ArrayList<>();
        List<SysMenu> menuChildren = childrenMap.getOrDefault(menu.getId(), Collections.emptyList());
        for (SysMenu child : menuChildren) {
            children.add(buildMenuNode(child, childrenMap, permissionByCode, menuPermissionMap));
        }

        List<MenuPermissionDetail> permissions = menuPermissionMap.getOrDefault(menu.getId(), Collections.emptyList());
        for (MenuPermissionDetail detail : permissions) {
            if (detail.getCode() != null && detail.getCode().startsWith("menu:")) {
                continue;
            }
            children.add(toPermissionNode(detail));
        }

        if (!children.isEmpty()) {
            node.setChildren(children);
        }
        return node;
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

    private PermissionTreeNode toPermissionNode(MenuPermissionDetail detail) {
        PermissionTreeNode node = new PermissionTreeNode();
        node.setId(detail.getId());
        node.setTitle(detail.getName() + " (" + detail.getCode() + ")");
        return node;
    }

    private List<MenuPermissionDetail> collectOrphanActions(List<MenuPermissionDetail> menuPermissionDetails,
                                                            Map<String, SysPermission> permissionByCode) {
        List<MenuPermissionDetail> orphan = new ArrayList<>();
        for (SysPermission permission : permissionByCode.values()) {
            if (permission.getCode() == null || permission.getCode().startsWith("menu:")) {
                continue;
            }
            boolean assigned = menuPermissionDetails.stream()
                    .anyMatch(detail -> Objects.equals(detail.getId(), permission.getId()));
            if (!assigned) {
                MenuPermissionDetail detail = new MenuPermissionDetail();
                detail.setId(permission.getId());
                detail.setCode(permission.getCode());
                detail.setName(permission.getName());
                orphan.add(detail);
            }
        }
        return orphan;
    }
}
