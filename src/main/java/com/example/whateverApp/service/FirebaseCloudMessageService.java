package com.example.whateverApp.service;

import com.example.whateverApp.dto.FcmMessage;
import com.example.whateverApp.dto.FcmTest;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.Alarm;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.jpaRepository.AlarmRepository;
import com.example.whateverApp.repository.mongoRepository.ConversationRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FirebaseCloudMessageService {

    private final String API_URL = "https://fcm.googleapis.com/v1/projects/real-d0a66/messages:send";
    private final String serverKey = "key=AAAAc3C3m2U:APA91bFG4TIhiUxQHWBAILV39VbUdyahZfh3LU8JFWoPDwfK7rmce4uI3nvYrvIK9u3XRQwwLG0jtZrUddYeNtIEpO6T82vMEawHyTOUYctGS99_JWkvikHM6EVKE92hFeV4K5XMNQdQ";
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



    public Boolean sendMessageGroup(List<User> userList, String title, String body) throws IOException {
        List<String> strings = new ArrayList<>();
        for (User user : userList) {
            strings.add(user.getNotificationToken());
        }
        String to = UUID.randomUUID().toString();

        FcmTest build = FcmTest.builder()
                .operation("create")
                .notification_key_name(to)
                .registration_ids(strings).build();

        String message = objectMapper.writeValueAsString(build);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message,
                MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/notification")
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "key=AAAAc3C3m2U:APA91bFG4TIhiUxQHWBAILV39VbUdyahZfh3LU8JFWoPDwfK7rmce4uI3nvYrvIK9u3XRQwwLG0jtZrUddYeNtIEpO6T82vMEawHyTOUYctGS99_JWkvikHM6EVKE92hFeV4K5XMNQdQ")
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .addHeader("project_id", "495812320101")
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        JSONObject jsonResponse = new JSONObject(responseBody);
        String notificationKeyName = jsonResponse.optString("notification_key");


        // 그룹 메시지 생성
        JSONObject messageBody = new JSONObject();
        messageBody.put("to", notificationKeyName);
        JSONObject data = new JSONObject();
        data.put("title", title);
        data.put("body", body);
        messageBody.put("data", data);

        RequestBody requestBody2 = RequestBody.create(messageBody.toString(),
                MediaType.get("application/json; charset=utf-8"));

        Request request2 = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody2)
                .addHeader(HttpHeaders.AUTHORIZATION, serverKey)
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response2 = client.newCall(request2).execute();
        String responseBody2 = response2.body().string();
        System.out.println(responseBody2);

        if(response2.isSuccessful())
            return true;

        return false;
    }

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

    private String makeMessage(String targetToken, String title, String body) throws JsonProcessingException {
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

    public String test(List<User> userList, String title, String body) throws IOException{
        List<String> strings = new ArrayList<>();
        for (User user : userList) {
            strings.add(user.getNotificationToken());
        }
        String to = UUID.randomUUID().toString();
        String serverKey = "key=AAAAc3C3m2U:APA91bFG4TIhiUxQHWBAILV39VbUdyahZfh3LU8JFWoPDwfK7rmce4uI3nvYrvIK9u3XRQwwLG0jtZrUddYeNtIEpO6T82vMEawHyTOUYctGS99_JWkvikHM6EVKE92hFeV4K5XMNQdQ";

        FcmTest build = FcmTest.builder().operation("create").notification_key_name(to).registration_ids(strings).build();
        String message = objectMapper.writeValueAsString(build);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message,
                MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/notification")
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "key=AAAAc3C3m2U:APA91bFG4TIhiUxQHWBAILV39VbUdyahZfh3LU8JFWoPDwfK7rmce4uI3nvYrvIK9u3XRQwwLG0jtZrUddYeNtIEpO6T82vMEawHyTOUYctGS99_JWkvikHM6EVKE92hFeV4K5XMNQdQ")
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .addHeader("project_id", "495812320101")
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        JSONObject jsonResponse = new JSONObject(responseBody);
        String notificationKeyName = jsonResponse.optString("notification_key");


        // 그룹 메시지 생성
        JSONObject messageBody = new JSONObject();
        messageBody.put("to", notificationKeyName);
        JSONObject data = new JSONObject();
        data.put("title", "Group Message Title");
        data.put("body", "This is a group message.");
        messageBody.put("data", data);

        RequestBody requestBody2 = RequestBody.create(messageBody.toString(),
                MediaType.get("application/json; charset=utf-8"));

        Request request2 = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody2)
                .addHeader(HttpHeaders.AUTHORIZATION, serverKey)
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response2 = client.newCall(request2).execute();
        String responseBody2 = response2.body().string();
        System.out.println(responseBody2);
        return "ok";

    }

}