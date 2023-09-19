package com.example.whateverApp.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class StompErrorHandler extends StompSubProtocolErrorHandler {

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