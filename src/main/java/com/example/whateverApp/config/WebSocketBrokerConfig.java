package com.example.whateverApp.config;

import com.example.whateverApp.handler.StompErrorHandler;
import com.example.whateverApp.handler.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;
    private final StompErrorHandler stompErrorHandler;
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/pub"); // /pub 경로로 시작하는 STOMP SEND 요청 메시지의 "destination" 헤더는 @Controller 객체의 @MessageMapping 메서드로 라우팅 된다. -> publish
        registry.enableSimpleBroker("/topic", "/queue"); // 접두사가 붙은 url을 구독하는 대상들에 한하여 브로커가 메시지 전달 ->subscribe. 메시지를 전달하는 간단한 작업을 수행.
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
        registry.setErrorHandler(stompErrorHandler);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);

    }
}
