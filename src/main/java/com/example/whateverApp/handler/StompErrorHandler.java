package com.example.whateverApp.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeType;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class StompErrorHandler extends StompSubProtocolErrorHandler {

    /**
     * 클라이언트 메시지 처리 중에 발생한 오류를 처리
     *
     * @param clientMessage 클라이언트 메시지
     * @param ex 발생한 예외
     * @return 오류 메시지를 포함한 Message 객체
     */
    @Override
    public Message<byte[]> handleClientMessageProcessingError(
            Message<byte[]> clientMessage,
            Throwable ex) {

        if ("UNAUTHORIZED".equals(ex.getMessage())) {
            return errorMessage("UNAUTHORIZED", clientMessage);
        }
        else if ("ReIssueJwt".equals(ex.getMessage())){
            return errorMessage("ReIssueToken", clientMessage);
        }

        return super.handleClientMessageProcessingError(clientMessage, ex);
    }

    /**
     * 오류 메시지를 포함한 Message 객체를 생성
     *
     * @param errorMessage 오류 메시지
     * @return 오류 메시지를 포함한 Message 객체
     */
    private Message<byte[]> errorMessage(String errorMessage, Message<byte[]> clientMessage) {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setLeaveMutable(true);
        accessor.setNativeHeader("errorType", errorMessage);
        Set<String> strings = clientMessage.getHeaders().keySet();

        for (String string : strings)
            accessor.setNativeHeader(string, clientMessage.getHeaders().get(string).toString());

        return MessageBuilder.createMessage(clientMessage.getPayload(),
                accessor.getMessageHeaders());
    }

}