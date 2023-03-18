package com.example.whateverApp.service;


import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAuthenticationService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user =  userRepository.findByUserId(userId)
                                            .orElseThrow(()-> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return createUserDetails(user);
    }

    // 해당하는 User 의 데이터가 존재한다면 UserDetails 객체로 만들어서 리턴
    private UserDetails createUserDetails(User user) {

        return new org.springframework.security.core.userdetails.User(
                user.getUserId(),
                passwordEncoder.encode(user.getPassword()),
                List.of(new SimpleGrantedAuthority(user.getRoles().toString().substring(1, user.getRoles().toString().length()-1)))
        );

    }

}
