package com.example.whateverApp.controller;

import com.example.whateverApp.dto.TokenInfo;
import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.service.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.util.JsonMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static com.google.firebase.database.util.JsonMapper.serializeJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@Transactional
class AuthControllerTest{

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${jwt.secret}")
    private String secretKey;


    @Test
    @DisplayName("Join Test")
    void testJoin() throws Exception {
        User user = User.builder().userId("test").name("test").password("1234").longitude(123.4).latitude(567.8).build();
        UserDto userDto = new UserDto(user);


        MvcResult mvcResult = mvc.perform(post("/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serializeJson(userDto)))
                .andExpect(status().isOk())
                .andReturn();

        Object o = JsonMapper.parseJsonValue(mvcResult.getResponse().getContentAsString());
        UserDto resultDto = objectMapper.convertValue(o, UserDto.class);
        Assertions.assertThat(resultDto.getName()).isEqualTo(userDto.getName());
        Assertions.assertThat(resultDto.getPassword()).isEqualTo(userDto.getPassword());

    }

    @Test
    @DisplayName("Login Test")
    void login() throws Exception {

        testJoin();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", "test");
        jsonObject.put("password", "1234");

        MvcResult mvcResult = mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

    }

    @Test
    @DisplayName("Token Expire Test")
    void tokenExpiredTest() throws Exception {


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", "test");
        jsonObject.put("password", "1234");

        String token = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)),
                        SignatureAlgorithm.HS256)
                .setSubject(String.valueOf(1L))
                .setExpiration(new Date((new Date()).getTime() - 1))
                .compact();


        MvcResult mvcResult = mvc.perform(post("/api/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString())
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isForbidden())
                .andReturn();
    }


//    @Test
//    void issueToken() throws Exception {
//        login();
//        MvcResult mvcResult = mvc.perform(put("/token"))
//                .andReturn();
//        TokenInfo tokenInfo = (TokenInfo) JsonMapper.parseJsonValue(mvcResult.getResponse().getContentAsString());
//
//        Assertions.assertThat(mvcResult.getResponse()).isNotNull();
//        Assertions.assertThat(jwtTokenProvider.validateToken(tokenInfo.getAccessToken())).isTrue();
//    }
}