package com.example.whateverApp.jwt;

import com.example.whateverApp.dto.TokenInfo;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;
    private final UserRepository userRepository;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, UserRepository userRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.userRepository = userRepository;
    }

    // 유저 정보를 가지고 AccessToken, RefreshToken 을 생성하는 메서드
    public TokenInfo generateToken(Authentication authentication, HttpServletResponse response) {
        // 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + 86400000); //1800000 -> 토큰 유효기간 30분 = 30*60*1000 개발환경에서는 높게 해놓음.
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)//
                .setExpiration(accessTokenExpiresIn) //유효기간 설정
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + 86400000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setMaxAge(7*24*60*60);
        cookie.setPath("/");
        //cookie.setSecure(true); //로컬환경에서는 Secure설정을 꺼놔야 함. secure가 켜지면 https환경에서만 쿠키가 전달됨.
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        User user = userRepository.findByUserId(authentication.getName()).get();
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken("httpOnly")
                .build();
    }

    // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public Optional<User> getUser(HttpServletRequest request){
        String accessToken = resolveToken(request);
        Authentication authentication = getAuthentication(accessToken);
        return userRepository.findByUserId(authentication.getName());
    }

    // Request Cookie 에서 토큰 정보 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }


    //여기 문법 좀 많이 고쳐야됨. 어려움 ㅠㅠ
    @Transactional
    public TokenInfo reissueToken(String refreshToken, HttpServletResponse response) throws RuntimeException{
        User user=null;
        String findRefreshToken;
        //만약 값이 있으면 ? user의 refreshToken과 쿠키의 refreshToken을 비교.
        Optional<User> finduser = userRepository.findByRefreshToken(refreshToken);

        if(finduser.isPresent()){
            user = finduser.get();
            findRefreshToken = user.getRefreshToken();
        }
        else{
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(user.getRoles().toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        UserDetails principal = new org.springframework.security.core.userdetails.User(user.getUsername(), "", authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUserId(), authorities);
        if(findRefreshToken.equals(refreshToken)){
            // 새로운거 생성
            TokenInfo newToken = generateToken(authentication, response);
            return newToken;
        }
        else {
            log.info("refresh 토큰이 일치하지 않습니다. ");
            return null;
        }
    }


    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
