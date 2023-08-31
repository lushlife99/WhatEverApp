package com.example.whateverApp.service;

import com.example.whateverApp.controller.LocationController;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.AccountStatus;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class LocationServiceImplTest {


    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private LocationServiceImpl locationService;
    @MockBean private LocationController locationController;
    @MockBean private MockHttpServletRequest request;
    @MockBean private UserServiceImpl userService;
    @Autowired private WorkRepository workRepository;
    @Autowired private UserRepository userRepository;


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
    void findHelper() throws MalformedURLException, IOException {

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

    @Test
    void joinAdmin(){
        User admin = User.builder()
                .userId("admin")
                .password("1234")
                .name("admin")
                .roles(Collections.singletonList("ROLE_ADMIN"))
                .imageFileName(UUID.randomUUID())
                .longitude(0.0)
                .latitude(0.0)
                .accountStatus(AccountStatus.USING)
                .build();
        Optional<User> byUserId = userRepository.findByUserId("admin");
        if(!byUserId.isPresent())
            userRepository.save(admin);

    }


}