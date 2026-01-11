package com.qss.pet.security;

import com.qss.pet.entity.SysPermission;
import com.qss.pet.entity.SysRole;
import com.qss.pet.entity.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SecurityUser implements UserDetails {
    private final SysUser user;
    private final List<SysRole> roles;
    private final List<SysPermission> permissions;

    public SecurityUser(SysUser user, List<SysRole> roles, List<SysPermission> permissions) {
        this.user = user;
        this.roles = roles;
        this.permissions = permissions;
    }

    public SysUser getUser() {
        return user;
    }

    public List<SysRole> getRoles() {
        return roles;
    }

    public List<SysPermission> getPermissions() {
        return permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (roles != null) {
            for (SysRole role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            }
        }
        if (permissions != null) {
            for (SysPermission permission : permissions) {
                authorities.add(new SimpleGrantedAuthority(permission.getCode()));
            }
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != null && user.getStatus() == 1;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() != null && user.getStatus() == 1;
    }
}
