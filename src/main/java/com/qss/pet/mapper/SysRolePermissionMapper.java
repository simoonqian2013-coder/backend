package com.qss.pet.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysRolePermissionMapper {

    @Insert("""
            INSERT INTO sys_role_permission (role_id, permission_id)
            VALUES (#{roleId}, #{permissionId})
            """)
    int insertRolePermission(Long roleId, Long permissionId);

    @Delete("""
            DELETE FROM sys_role_permission
            WHERE role_id = #{roleId} AND permission_id = #{permissionId}
            """)
    int deleteRolePermission(Long roleId, Long permissionId);

    @Delete("""
            DELETE FROM sys_role_permission
            WHERE role_id = #{roleId}
            """)
    int deletePermissionsByRoleId(Long roleId);
}
