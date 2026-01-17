package com.qss.pet.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserRoleMapper {

    @Insert("""
            INSERT INTO sys_user_role (user_id, role_id)
            VALUES (#{userId}, #{roleId})
            """)
    int insertUserRole(Long userId, Long roleId);

    @Delete("""
            DELETE FROM sys_user_role
            WHERE user_id = #{userId} AND role_id = #{roleId}
            """)
    int deleteUserRole(Long userId, Long roleId);

    @Delete("""
            DELETE FROM sys_user_role
            WHERE user_id = #{userId}
            """)
    int deleteRolesByUserId(Long userId);

    @Delete("""
            DELETE FROM sys_user_role
            WHERE role_id = #{roleId}
            """)
    int deleteUsersByRoleId(Long roleId);
}
