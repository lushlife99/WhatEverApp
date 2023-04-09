package com.example.whateverApp.handler;

import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChatPreHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        String authorizationHeader = String.valueOf(headerAccessor.getNativeHeader("Authorization"));
        if(authorizationHeader == null || authorizationHeader.equals("null")){
            throw new MessageDeliveryException("토큰이 없습니다.");
        }
         String token = authorizationHeader.substring(BEARER_PREFIX.length());
        System.out.println(token);
        jwtTokenProvider.validateToken(token);

        return message;
    }
}