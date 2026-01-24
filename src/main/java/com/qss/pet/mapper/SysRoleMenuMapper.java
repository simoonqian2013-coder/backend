package com.qss.pet.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper {

    @Insert("""
            INSERT INTO sys_role_menu (role_id, menu_id)
            VALUES (#{roleId}, #{menuId})
            """)
    int insertRoleMenu(Long roleId, Long menuId);

    @Delete("""
            DELETE FROM sys_role_menu
            WHERE role_id = #{roleId}
            """)
    int deleteMenusByRoleId(Long roleId);

    @Delete("""
            DELETE FROM sys_role_menu
            WHERE menu_id = #{menuId}
            """)
    int deleteRolesByMenuId(Long menuId);

    @Select("""
            <script>
            SELECT menu_id
            FROM sys_role_menu
            WHERE role_id IN
            <foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>
                #{roleId}
            </foreach>
            </script>
            """)
    List<Long> selectMenuIdsByRoleIds(@Param("roleIds") List<Long> roleIds);

    @Select("""
            SELECT role_id
            FROM sys_role_menu
            WHERE menu_id = #{menuId}
            """)
    List<Long> selectRoleIdsByMenuId(Long menuId);
}
