package com.qss.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qss.pet.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} LIMIT 1")
    SysUser selectByUsername(String username);
}
