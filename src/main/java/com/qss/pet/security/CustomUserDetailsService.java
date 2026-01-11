package com.qss.pet.security;

import com.qss.pet.entity.SysPermission;
import com.qss.pet.entity.SysRole;
import com.qss.pet.entity.SysUser;
import com.qss.pet.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        List<SysRole> roles = userService.listRolesByUserId(user.getId());
        List<SysPermission> permissions = userService.listPermissionsByUserId(user.getId());
        return new SecurityUser(user, roles, permissions);
    }
}
