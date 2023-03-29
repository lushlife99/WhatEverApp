//package com.example.whateverApp.config;
//
//import com.example.whateverApp.handler.SocketTextHandler;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
//import org.springframework.web.socket.sockjs.transport.handler.SockJsWebSocketHandler;
//
//@Configuration
//@EnableWebSocket
//public class WebSocketConfig implements WebSocketConfigurer {
//
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(socketTextHandler(), "/ws")
//                .addInterceptors(new HttpSessionHandshakeInterceptor())
//                .setAllowedOriginPatterns("*");
//    }
//
//    public WebSocketHandler socketTextHandler(){
//        return new SocketTextHandler();
//    }
//}
