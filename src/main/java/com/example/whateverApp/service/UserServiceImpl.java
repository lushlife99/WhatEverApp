package com.example.whateverApp.service;

import com.example.whateverApp.dto.TokenInfo;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.service.interfaces.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.el.parser.Token;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder;

    public TokenInfo login(User user, HttpServletResponse response){
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUserId(), user.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 매서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        Authentication authentication = authenticationManagerBuilder.authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication, response);
        return tokenInfo;
    }

    public Boolean join(User user){
        if(userRepository.findByUserId(user.getUserId()).isPresent()){
            return false;
        }
        user.setRoles(Collections.singletonList("ROLE_USER"));
        userRepository.save(user);
        return true;
    }

    public TokenInfo issueToken(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        String refreshToken="";
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("refreshToken")){
                refreshToken = cookie.getValue();
            }
        }

        return jwtTokenProvider.reissueToken(refreshToken, response);
    }




    @Override
    public User update(User user) {
        return null;
    }

    @Override
    public User delete(HttpServletRequest request) {
        return null;
    }

    @Override
    public User get(HttpServletRequest request) {
        return null;
    }

    @Override
    public User getPurchaseList(HttpServletRequest request) {
        return null;
    }

    @Override
    public User getSellList(HttpServletRequest request) {
        return null;
    }

    @Override
    public List<User> getByHighRating(HttpServletRequest request) {
        return null;
    }

    @Override
    public List<User> getByCloseDistance(HttpServletRequest request) {
        return null;
    }
}
