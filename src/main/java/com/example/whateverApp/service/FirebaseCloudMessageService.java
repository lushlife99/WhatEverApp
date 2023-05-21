package com.example.whateverApp.service;

import com.example.whateverApp.dto.FcmMessage;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.repository.ConversationRepository;
import com.example.whateverApp.repository.UserRepository;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FirebaseCloudMessageService {

    private final String API_URL = "https://fcm.googleapis.com/v1/projects/fir-c2b7d/messages:send";
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    public void sendMessageTo(String targetToken, String title, String body) throws IOException {
        String message = makeMessage(targetToken, title, body);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message,
                MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
    }

    public void chatNotification(String conversationId) throws IOException {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(()-> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        List<Chat> chatList = conversation.getChatList();
        Chat chat = chatList.get(chatList.size() - 1);
        String title = chat.getSenderName();
        String body = chat.getMessage();
        String notificationToken;

        if(!conversation.getCreatorName().equals(title))
            notificationToken = userRepository.findById(conversation.getCreatorId()).get().getNotificationToken();
        else
            notificationToken = userRepository.findById(conversation.getParticipantId()).get().getNotificationToken();

        sendMessageTo(notificationToken, title, body);
    }

    private String makeMessage(String targetToken, String title, String body) throws JsonParseException, JsonProcessingException {
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .image(null)
                                .build()
                        ).build()).validateOnly(false).build();

        return objectMapper.writeValueAsString(fcmMessage);
    }

    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "firebase/firebase_service_key.json";

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}