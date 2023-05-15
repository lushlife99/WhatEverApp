package com.example.whateverApp.service;

import com.example.whateverApp.dto.TokenInfo;
import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.Enum.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.service.interfaces.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

        Authentication authentication = authenticationManagerBuilder.authenticate(authenticationToken);
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication, response);
        User findUser = userRepository.findByUserId(user.getUserId())
                .orElseThrow(() ->new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        tokenInfo.setId(findUser.getId());
        return tokenInfo;
    }

    public User join(User user) {
        if (userRepository.findByUserId(user.getUserId()).isPresent())
            throw new CustomException(ErrorCode.DUPLICATE_USER);

        user.setRoles(Collections.singletonList("ROLE_USER"));
        user.setImageFileName(UUID.randomUUID());
        return userRepository.save(user);
    }

    @Override
    public UserDto getUserInfo(HttpServletRequest request) throws MalformedURLException, IOException{
        User findUser = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        UserDto userDto = new UserDto(findUser);
        // 사진파일 보내기.
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] photoEncode;
        File file = new File(fileDir + findUser.getImageFileName());
        if (file.exists()) {
            photoEncode = encoder.encode(new UrlResource("file:" + fileDir + findUser.getImageFileName()).getContentAsByteArray());
            userDto.setImage(new String(photoEncode, "UTF8"));
        }
        return userDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDto update(UserDto userDto, HttpServletRequest request) {
        User findUser = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        findUser.updateUserInfo(userDto);
        userRepository.save(findUser);
        return new UserDto(findUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public User updateProfileImage(HttpServletRequest request, MultipartFile file) throws IOException {;
        User user = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        file.transferTo(new File(fileDir + user.getImageFileName()));

        return user;
    }

//    public Resource getUserImage(HttpServletRequest request) throws MalformedURLException {
//        String accessToken = request.getHeader("Authorization").substring(7);
//        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
//        String userId = authentication.getName();
//        Optional<User> userOptional = userRepository.findByUserId(userId);
//        User user = userOptional.get();
//        return new UrlResource(fileDir + user.getImageFileName());
//    }

    /**
     * 이거 될 진 모르겠음. 일단 해보자.
     * 위에 있는 getUserImage 먼저 해보고 잘 되면 아래꺼 해보자.
     */

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

    public Location setUserLocation(Location location, HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        user.setLatitude(location.getLatitude());
        user.setLongitude(location.getLongitude());
        userRepository.save(user);
        return location;
    }

    public String updateNotificationToken(String notificationToken, HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        user.setNotificationToken(notificationToken);
        return notificationToken;
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
