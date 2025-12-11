package com.doan.WEB_TMDT.security;

import com.doan.WEB_TMDT.module.auth.entity.User;
import com.doan.WEB_TMDT.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + email));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Quyền cấp 1 (Role) - Thêm cả có và không có prefix ROLE_
        authorities.add(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));
        authorities.add(new SimpleGrantedAuthority(u.getRole().name()));

        // Quyền cấp 2 (Position) - Cho Employee
        if (u.getEmployee() != null && u.getEmployee().getPosition() != null) {
            authorities.add(new SimpleGrantedAuthority(u.getEmployee().getPosition().name()));
        }

        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),
                u.getPassword(),
                authorities
        );
    }
}
