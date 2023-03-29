//package com.example.whateverApp.handler;
//
//
//import lombok.extern.log4j.Log4j2;
//import org.bson.json.JsonObject;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.*;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//@Log4j2
//public class SocketTextHandler extends TextWebSocketHandler {
//    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
//
//
//
////    @Override
////    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
////        String payload = message.getPayload();
////        log.info(payload);
////        JsonObject jsonObject = new JsonObject(payload);
////        for (WebSocketSession s : sessions) {
////            s.sendMessage(new TextMessage("Hi !!!!"));
////        }
////   }
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        sessions.add(session);
//        log.info(session + "클라이언트 접속");
//
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        sessions.remove(session);
//    }
//}
