package com.example.whateverApp.service;

import com.example.whateverApp.dto.TokenInfo;
import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.AccountStatus;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;


@RequiredArgsConstructor
@Service
public class UserServiceImpl {

    @Value("${file:}")
    private String fileDir;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final WorkRepository workRepository;
    private final PasswordEncoder passwordEncoder;

    public TokenInfo login(User user, HttpServletResponse response) {
        User findUser = get(user.getUserId());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUserId(), user.getPassword());
        Authentication authentication = authenticationManagerBuilder.authenticate(authenticationToken);

        if(!passwordEncoder.matches(user.getPassword(), findUser.getPassword()))
            throw new CustomException(ErrorCode.MISMATCH_PASSWORD);

        if(!findUser.isAccountNonLocked())
            if(findUser.getAccountStatus().equals(AccountStatus.BAN))
                throw new LockedException(makeMessage(findUser));
            else throw new LockedException("계정이 영구 정지 당했습니다. ");

        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication, response);
        tokenInfo.setId(findUser.getId());
        return tokenInfo;
    }

    public UserDto join(User user) {
        if (userRepository.findByUserId(user.getUserId()).isPresent())
            throw new CustomException(ErrorCode.DUPLICATE_USER);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singletonList("ROLE_USER"));
        user.setAccountStatus(AccountStatus.USING);
        user.setImageFileName(UUID.randomUUID());
        return new UserDto(userRepository.save(user));
    }

    public UserDto getMyInfo(HttpServletRequest request) throws IOException{
        User findUser = jwtTokenProvider.getUser(request);

        UserDto userDto = new UserDto(findUser);
        userDto.setImage(getUserImage(findUser));
        return userDto;
    }

    public UserDto getUserInfo(Long userId) throws IOException {
        User findUser = get(userId);
        UserDto userDto = new UserDto(findUser);
        userDto.setImage(getUserImage(findUser));
        return userDto;
    }

    public String getUserImage(User findUser) throws IOException {
        Base64.Encoder encoder = Base64.getEncoder();
        File file = new File(fileDir + findUser.getImageFileName());

        if (file.exists())
            return new String(encoder.encode(new UrlResource("file:" + fileDir + findUser.getImageFileName()).getContentAsByteArray()), "UTF8");
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public UserDto update(UserDto userDto, HttpServletRequest request) {
        User findUser = jwtTokenProvider.getUser(request);

        findUser.updateUserInfo(userDto);
        userRepository.save(findUser);
        return new UserDto(findUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public User updateProfileImage(HttpServletRequest request, MultipartFile file) throws IOException {;
        User user = jwtTokenProvider.getUser(request);

        file.transferTo(new File(fileDir + user.getImageFileName()));
        return user;
    }
    public TokenInfo issueToken(HttpServletRequest request, HttpServletResponse response) {
        User user = jwtTokenProvider.getUser(request);
        Cookie[] cookies = request.getCookies();
        String refreshToken = "";

        if(!user.isAccountNonLocked())
            throw new CustomException(ErrorCode.BANNED_ACCOUNT);

        for (Cookie cookie : cookies)
            if (cookie.getName().equals("refreshToken"))
                refreshToken = cookie.getValue();

        return jwtTokenProvider.reissueToken(refreshToken, response);
    }

    public Location setUserLocation(Location location, HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request);

        user.updateLocation(location);
        userRepository.save(user);
        return location;
    }

    public String updateNotificationToken(String notificationToken, HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request);

        user.setNotificationToken(notificationToken);
        userRepository.save(user);
        return notificationToken;
    }

    @Transactional
    public void delete(HttpServletRequest request) {
        User user = jwtTokenProvider.getUser(request);
        userRepository.delete(user);
    }

    public void setAvgReactTime(Work work, Conversation conversation){
        User helper = work.getHelper();
        List<Work> workListByHelper = workRepository.findByHelper(helper);
        int totalSize = workListByHelper.size() + 1;
        LocalDateTime sendTime = conversation.getChatList().get(0).getSendTime();

        if(helper.getAvgReactTime() == 1000000000L){
            helper.setAvgReactTime(ChronoUnit.MINUTES.between(sendTime, LocalDateTime.now()));
        }
        else {
            Long avgReactTime = (helper.getAvgReactTime() * workListByHelper.size() + ChronoUnit.MINUTES.between(sendTime, LocalDateTime.now())) / totalSize;
            helper.setAvgReactTime(avgReactTime);
        }

        userRepository.save(helper);
    }

    public UserDto modifyPassword(String password, HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request);
        user.setPassword(password);
        userRepository.save(user);

        return new UserDto(user);
    }

    private String makeMessage(User banUser){
        return new String("계정이 잠겼습니다.\n" +
                banUser.getAccountReleaseTime().format(DateTimeFormatter.ofPattern("yy-MM-dd HH:mm")) +
                " 이후에 다시 시도하세요.");
    }

    public User get(Long userId){
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public User get(String userId){
        return userRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }


}
