package com.example.whateverApp.service;

import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.ConversationRepository;
import com.example.whateverApp.repository.HelperLocationRepository;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.repository.WorkRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WorkServiceImplTest {

    @InjectMocks
    private WorkServiceImpl workService;
    @Mock private WorkRepository workRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserRepository userRepository;
    @Mock private HelperLocationRepository helperLocationRepository;
    @Mock private AlarmService alarmService;
    @Mock private SimpMessagingTemplate simpMessagingTemplate;
    @Mock private ConversationRepository conversationRepository;
    @Mock private MockHttpServletRequest request;
    @Mock private MockHttpServletResponse response;
    @Mock private MockHttpSession session;
    private static final double EARTH_RADIUS = 6371;

    private String secretKey = "VlwEyVBsYt9V7zq57TejMnVUyzblYcfPQye08f7MGVA9XkHa";
    private final String userId = "test";
    private final String password = "1234";

    @BeforeEach
    public void setUp() throws Exception {
        request = new MockHttpServletRequest();
        session = new MockHttpSession();

        String token = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)),
                        SignatureAlgorithm.HS256)
                .setSubject(userId)
                .setExpiration(new Date((new Date()).getTime() + 1000))
                .compact();
        request.addHeader("Authorization", "Bearer "+token);

    }

    @Test
    @DisplayName("Create Test")
    void create() {
        //given
        Long fakeWorkId = 1L;
        Long fakeUserId = 2L;
        User user = User.builder().userId(userId).password(password).roles(Collections.singletonList("ROLE_USER")).id(fakeUserId).build();
        Work work = Work.builder().id(fakeWorkId).title("title").context("context").longitude(123.4).latitude(456.7).customer(user).receiveLatitude(0.0).receiveLongitude(0.0).build();

        WorkDto requestDto = new WorkDto(work);


        // mocking
        given(workRepository.save(work))
                .willReturn(work);
        given(workRepository.findById(fakeWorkId))
                .willReturn(Optional.ofNullable(work));
        given(jwtTokenProvider.getUser(request))
                .willReturn(Optional.ofNullable(user));

        //when
        WorkDto responseDto = workService.create(requestDto, request);


        //then
        Work findWork = workRepository.findById(1L).get();

        assertEquals(requestDto.getTitle(), responseDto.getTitle());
        assertEquals(requestDto.getContext(), requestDto.getContext());

    }

    @Test
    void update() {
    }

    @Test
    void matchingHelper() {
    }
}