package com.example.whateverApp.service;

import com.example.whateverApp.dto.TokenInfo;
import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.jwt.JwtAuthenticationFilter;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.service.interfaces.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;


@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${file:}")
    private String fileDir;

    public TokenInfo login(User user, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUserId(), user.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 매서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        Authentication authentication = authenticationManagerBuilder.authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication, response);
        return tokenInfo;
    }

    public Boolean join(User user) {
        if (userRepository.findByUserId(user.getUserId()).isPresent()) {
            return false;
        }
        user.setRoles(Collections.singletonList("ROLE_USER"));
        user.setImageFileName(UUID.randomUUID());
        userRepository.save(user);
        return true;
    }

    @Override
    public UserDto getUserInfo(HttpServletRequest request) {
        Authentication authorization = jwtTokenProvider.getAuthentication(request.getHeader("Authorization").substring(7));
        User findUser = userRepository.findByUserId(authorization.getName()).get();
        return new UserDto(findUser);
    }

    @Override
    public UserDto update(UserDto userDto, HttpServletRequest request) {
        Authentication authorization = jwtTokenProvider.getAuthentication(request.getHeader("Authorization").substring(7));
        User findUser = userRepository.findByUserId(authorization.getName()).get();
        findUser.updateUserInfo(userDto);
        userRepository.save(findUser);
        return new UserDto(findUser);
    }

    @Transactional
    public User updateProfileImage(HttpServletRequest request, MultipartFile file) throws IOException {
        String accessToken = request.getHeader("Authorization").substring(7);
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        String userId = authentication.getName();
        Optional<User> userOptional = userRepository.findByUserId(userId);
        User user = userOptional.get();
        System.out.println(user.getImageFileName());
        file.transferTo(new File(fileDir + user.getImageFileName()));
        return user;
    }

    public Resource getUserImage(HttpServletRequest request) throws MalformedURLException {
        String accessToken = request.getHeader("Authorization").substring(7);
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        String userId = authentication.getName();
        Optional<User> userOptional = userRepository.findByUserId(userId);
        User user = userOptional.get();
        return new UrlResource(fileDir + user.getImageFileName());
    }

    /**
     * 이거 될 진 모르겠음. 일단 해보자.
     * 위에 있는 getUserImage 먼저 해보고 잘 되면 아래꺼 해보자.
     */

    public UrlResource[] getUserImageList(Long[] userIdList) throws MalformedURLException {
        UrlResource[] urlResources = new UrlResource[userIdList.length];
        for (int i = 0; i < userIdList.length; i++) {

            UUID imageFileName = userRepository.findById(userIdList[i]).get().getImageFileName();
            File file = new File(fileDir + imageFileName);
            if (file.exists()) {
                urlResources[i] = new UrlResource(fileDir + imageFileName);
            } else {
                urlResources[i] = null;
            }
        }
        return urlResources;
    }


    public TokenInfo issueToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = "";
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refreshToken")) {
                refreshToken = cookie.getValue();
            }
        }
        return jwtTokenProvider.reissueToken(refreshToken, response);
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
