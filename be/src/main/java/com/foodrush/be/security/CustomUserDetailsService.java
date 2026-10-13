package com.foodrush.be.security;

import com.foodrush.be.entity.User;
import com.foodrush.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));
        // Throw here rather than relying on UserDetails.isEnabled() alone, so both
        // the login flow and the JWT filter surface the same reason to the client.
        if (!user.isActive()) {
            throw new DisabledException("Tài khoản đã bị khoá: " + username);
        }
        return UserPrincipal.from(user);
    }
}
