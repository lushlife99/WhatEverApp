package com.example.whateverApp.service;

import com.example.whateverApp.controller.LocationController;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.repository.WorkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;


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
        User user = userRepository.findByUserId("admin").get();
        ArrayList<String> strings = new ArrayList<>();
        strings.add("ROLE_ADMIN");
        user.setRoles(strings);
        user.setLatitude(0.0);
        user.setLongitude(0.0);
        userRepository.save(user);

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