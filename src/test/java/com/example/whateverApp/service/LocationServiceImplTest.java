package com.example.whateverApp.service;

import com.example.whateverApp.controller.LocationController;
import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.repository.WorkRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class LocationServiceImplTest {


    @Autowired private UserRepository userRepository;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @Autowired private LocationServiceImpl locationService;
    @MockBean private LocationController locationController;
    @MockBean private MockHttpServletRequest request;
    @MockBean private UserServiceImpl userService;
    @Autowired private WorkRepository workRepository;

    @BeforeEach
    @Transactional
      void BeforeEach(){
        User user = userRepository.findByUserId("admin").get();
        ArrayList<String> strings = new ArrayList<>();
        strings.add("ROLE_ADMIN");
        user.setRoles(strings);
        userRepository.save(user);
    }

//    @BeforeEach
//    @Transactional
//    void BeforeEach(){
//        List<User> all = userRepository.findAll();
//        Random random = new Random();
//        for (User user : all) {
//            user.setRating(random.nextInt(5)+random.nextDouble());
//            user.setAvgReactTime(random.nextInt(10));
//        }
//        userRepository.saveAll(all);
//    }

    @Test
    void findHelper() throws MalformedURLException, IOException { // 5km 안에 있는 헬퍼들을 찾아줌. Default = 거리순, 헬퍼 정렬 Option : 1, 거리 2, 별점, 3. 평균 첫 응답시간
        long count = userRepository.count();
        User standardUser = userRepository.findByUserId("a").get();
        Location location = new Location();
        location.setLatitude(standardUser.getLatitude());
        location.setLongitude(standardUser.getLongitude());

        Page<UserDto> helperList = locationService.findHelperByDistance(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "distance")), location, request);
        Assertions.assertThat(helperList.getTotalElements()).isEqualTo(3); //* 자기 자신까지 포함한 숫자임. 왜냐하면 테스트레이어에선 jwt검증이 안되기 때문에 인증이 되지 않았음. 그래서 자기 자신을 빼서 주는 로직을 빼놨음.
        Assertions.assertThat(helperList.getContent().get(0).getName()).isEqualTo("조선대학교 해오름관");
        Assertions.assertThat(helperList.getContent().get(1).getName()).isEqualTo("동명동");
        Assertions.assertThat(helperList.getContent().get(2).getName()).isEqualTo("진월동 행정복지센터");

    }

    @Test
    void getDistance() {

    }

    @Test
    void setUserLocation() {
    }

    @Test
    void setSellerLocation() {
    }

}