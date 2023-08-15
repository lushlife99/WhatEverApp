package com.example.whateverApp.service;

import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.document.HelperLocation;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.mongoRepository.ConversationRepository;
import com.example.whateverApp.repository.mongoRepository.HelperLocationRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
    private User mockUser;
    private Work work;



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

        Long fakeWorkId = 1L;
        Long fakeUserId = 2L;
        mockUser = User.builder().userId(userId).password(password).roles(Collections.singletonList("ROLE_USER")).id(fakeUserId).build();
        work = Work.builder().id(fakeWorkId).title("title").context("context").longitude(123.4).latitude(456.7).customer(mockUser).receiveLatitude(0.0).receiveLongitude(0.0).deadLineTime(1).build();
    }


    @Test
    @DisplayName("Work Entity Save Test")
    public void testCreateWork() throws IOException {
        // Given
        WorkDto workDto = new WorkDto(work);

        when(jwtTokenProvider.getUser(request)).thenReturn(Optional.of(mockUser));
        given(workRepository.save(work))
                .willReturn(work);

        // When
        WorkDto createdWorkDto = workService.create(workDto, request);

        // Then
        assertNotNull(createdWorkDto);
        assertEquals(workDto.getTitle(), createdWorkDto.getTitle());
        // ... 각 필드에 대한 검증을 추가로 작성

        verify(workRepository, times(1)).save(any(Work.class));
    }

    /**
     * work1 -> 이미 진행중인 심부름
     * work2 -> 막 만들어진 심부름
     * work3 -> 심부름이 끝나고 customer의 승인(걸제)를 대기중인 심부름
     *
     * 막 만들어진 심부름을 helper에게 보내기 위해 만들어진 함수이므로
     * work2만 return되는게 맞다.
     */
    @Test
    @DisplayName("Get Work List")
    public void testGetWork() throws Exception{

        // Given
        when(jwtTokenProvider.getUser(request)).thenReturn(Optional.of(mockUser));
        Work work1 = Work.builder().proceeding(true).finished(false).id(1L).createdTime(LocalDateTime.now()).finishedAt(LocalDateTime.now()).latitude(1.1).longitude(2.2).receiveLatitude(123.4).receiveLongitude(456.7).customer(mockUser).receiveLatitude(0.0).receiveLongitude(0.0).title("title").deadLineTime(1).context("context").build();
        Work work2 = Work.builder().proceeding(false).finished(false).id(2L).createdTime(LocalDateTime.now()).finishedAt(LocalDateTime.now()).latitude(1.1).longitude(2.2).receiveLatitude(123.4).receiveLongitude(456.7).customer(mockUser).receiveLatitude(0.0).receiveLongitude(0.0).title("title").deadLineTime(1).context("context").build();
        Work work3 = Work.builder().proceeding(false).finished(false).id(3L).createdTime(LocalDateTime.now()).finishedAt(LocalDateTime.now().plusHours(10)).latitude(1.1).longitude(2.2).receiveLatitude(123.4).receiveLongitude(456.7).customer(mockUser).receiveLatitude(0.0).receiveLongitude(0.0).title("title").deadLineTime(1).context("context").build();

        when(workRepository.findByCustomer(mockUser)).thenReturn(List.of(work1, work2));
        given(workRepository.save(any())).willReturn(work1, work2);
        workService.create(new WorkDto(work1), request);
        workService.create(new WorkDto(work2), request);
        // When
        List<WorkDto> workList = workService.getWorkList(request);

        // Then
        assertEquals(workList.size(), 1);
        assertEquals(workList.get(0).getId(), 2L);
    }

    @Test
    @DisplayName("Proceeding validate")
    public void testMatchingHelper1() {
        // Given
        WorkDto workDto = new WorkDto(work);
        HelperLocation helperLocation = HelperLocation.builder().workId(work.getId()).locationList(new ArrayList<>()).build();
        Conversation conversation = new Conversation();

        when(workRepository.findById(work.getId())).thenReturn(Optional.of(work));
        when(helperLocationRepository.save(helperLocation)).thenReturn(helperLocation);
        when(conversationRepository.findByWorkId(workDto.getId())).thenReturn(Optional.of(conversation));
        when(userRepository.findById(workDto.getHelperId())).thenReturn(Optional.of(mockUser));
        when(workRepository.save(work)).thenReturn(work);
        // When
        Work resultDto = workService.matchingHelper(workDto);

        // Then
        assertNotNull(resultDto);
        assertEquals(resultDto.isProceeding(), true);
    }

    @Test
    @DisplayName("Already Proceeding WorkDto, and Return Error 400")
    public void testMatchingHelper2() {
        // Given
        work.setProceeding(true);
        WorkDto workDto = new WorkDto(work);

        HelperLocation helperLocation = HelperLocation.builder().workId(work.getId()).locationList(new ArrayList<>()).build();
        Conversation conversation = new Conversation();

        when(workRepository.findById(work.getId())).thenReturn(Optional.of(work));
//        when(helperLocationRepository.save(helperLocation)).thenReturn(helperLocation);
//        when(conversationRepository.findByWorkId(workDto.getId())).thenReturn(Optional.of(conversation));
//        when(userRepository.findById(workDto.getHelperId())).thenReturn(Optional.of(mockUser));
//        when(workRepository.save(work)).thenReturn(work);

        // When
        CustomException customException = assertThrows(CustomException.class, () -> {
            Work resultDto = workService.matchingHelper(workDto);
        });

        assertEquals(customException.getErrorCode().toString(), "ALREADY_PROCEED_WORK");
        assertEquals(customException.getErrorCode().getHttpStatus().value(), 400);

    }



}