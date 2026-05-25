package com.flipkart.clone.security;

import com.flipkart.clone.entity.User;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String email;
    private final String password;
    private final boolean active;
    private final List<SimpleGrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.userId      = user.getId();
        this.email       = user.getEmail();
        this.password    = user.getPasswordHash() == null
                ? "" : user.getPasswordHash();
        this.active      = user.getIsActive();
        this.authorities = List.of(new SimpleGrantedAuthority(
                "ROLE_" + user.getRole().name()));
    }

    @Override public Collection<? extends org.springframework.security.core.GrantedAuthority>
    getAuthorities() { return authorities; }
    @Override public String getPassword()  { return password; }
    @Override public String getUsername()  { return email; }
    @Override public boolean isEnabled()   { return active; }
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
}