package com.qss.pet.mapper;

import com.qss.pet.dto.MenuPermissionDetail;
import com.qss.pet.dto.MenuPermissionView;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuPermissionMapper {

    @Insert("""
            INSERT INTO sys_menu_permission (menu_id, permission_id)
            VALUES (#{menuId}, #{permissionId})
            """)
    int insertMenuPermission(Long menuId, Long permissionId);

    @Delete("""
            DELETE FROM sys_menu_permission
            WHERE menu_id = #{menuId}
            """)
    int deletePermissionsByMenuId(Long menuId);

    @Select("""
            <script>
            SELECT mp.menu_id AS menuId, p.code AS code
            FROM sys_menu_permission mp
            JOIN sys_permission p ON p.id = mp.permission_id
            WHERE mp.menu_id IN
            <foreach collection='menuIds' item='menuId' open='(' separator=',' close=')'>
                #{menuId}
            </foreach>
            </script>
            """)
    List<MenuPermissionView> selectPermissionCodesByMenuIds(@Param("menuIds") List<Long> menuIds);

    @Select("""
            <script>
            SELECT mp.menu_id AS menuId, p.id AS id, p.code AS code, p.name AS name
            FROM sys_menu_permission mp
            JOIN sys_permission p ON p.id = mp.permission_id
            WHERE mp.menu_id IN
            <foreach collection='menuIds' item='menuId' open='(' separator=',' close=')'>
                #{menuId}
            </foreach>
            </script>
            """)
    List<MenuPermissionDetail> selectPermissionDetailsByMenuIds(@Param("menuIds") List<Long> menuIds);
}
