package com.example.whateverApp.service;

import com.example.whateverApp.dto.FcmMessage;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.Alarm;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.AlarmRepository;
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

    private final String API_URL = "https://fcm.googleapis.com/v1/projects/real-d0a66/messages:send";
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final AlarmRepository alarmRepository;

    public void sendMessageTo(User user, String title, String body) throws IOException {
        String targetToken = user.getNotificationToken();
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

        if(response.isSuccessful()){
            Alarm alarm = Alarm.builder()
                    .user(user)
                    .title(title)
                    .body(body)
                    .build();

            alarmRepository.save(alarm);
        }

        System.out.println(response.body().string());
    }

    /**
     * sendMessageGroup -> 단체 메시지 전송..
     * 일단 어려워서 주석처리.
     * @param conversationId
     * @throws IOException
     */

//    public void sendMessageGroup(String[] notificationArray, String title, String body) {
//        String message = makeMessage(targetToken, title, body);
//
//        OkHttpClient client = new OkHttpClient();
//        RequestBody requestBody = RequestBody.create(message,
//                MediaType.get("application/json; charset=utf-8"));
//        Request request = new Request.Builder()
//                .url(API_URL)
//                .post(requestBody)
//                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
//                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
//                .build();
//    }

    public void chatNotification(String conversationId) throws IOException {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(()-> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        List<Chat> chatList = conversation.getChatList();
        Chat chat = chatList.get(chatList.size() - 1);
        String title = chat.getSenderName();
        String body = chat.getMessage();
        User findUser;

        if(!conversation.getCreatorName().equals(title))
            findUser = userRepository.findById(conversation.getCreatorId()).get();

        else
            findUser = userRepository.findById(conversation.getParticipantId()).get();

        sendMessageTo(findUser, title, body);
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