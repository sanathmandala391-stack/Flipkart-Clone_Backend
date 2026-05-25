package com.flipkart.clone.security;

import com.flipkart.clone.entity.User;
import com.flipkart.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));

        // Spring Security needs GrantedAuthority — we prefix with ROLE_
//        return new org.springframework.security.core.userdetails.User(
//                user.getEmail(),
//                user.getPasswordHash() == null ? "" : user.getPasswordHash(),
//                user.getIsActive(),   // enabled
//                true,                 // accountNonExpired
//                true,                 // credentialsNonExpired
//                true,                 // accountNonLocked
//                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
//        );

        return new CustomUserDetails(user);
    }
}