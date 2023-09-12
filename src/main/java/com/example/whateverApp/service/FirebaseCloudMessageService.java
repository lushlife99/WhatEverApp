package com.example.whateverApp.service;

import com.example.whateverApp.dto.FcmMessage;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.model.NotificationBody;
import com.example.whateverApp.model.RouteOptions;
import com.example.whateverApp.model.WorkProceedingStatus;
import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.*;
import com.example.whateverApp.repository.jpaRepository.AlarmRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import com.example.whateverApp.repository.mongoRepository.ConversationRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FirebaseCloudMessageService {

    private final String API_URL = "https://fcm.googleapis.com/v1/projects/real-d0a66/";
    private final String serverKey = "key=AAAAc3C3m2U:APA91bFG4TIhiUxQHWBAILV39VbUdyahZfh3LU8JFWoPDwfK7rmce4uI3nvYrvIK9u3XRQwwLG0jtZrUddYeNtIEpO6T82vMEawHyTOUYctGS99_JWkvikHM6EVKE92hFeV4K5XMNQdQ";
    private final String notificationTitle = "WhatEverApp";
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final AlarmService alarmService;
    private final LocationServiceImpl locationService;
    private final WorkRepository workRepository;

    @Value("${file:}")
    private String fileDir;
    public void sendMessageTo(User user, String title, String body, FcmMessage.Data data) throws IOException {
        if(user.isAccountNonLocked() == false)
            return;

        String targetToken = user.getNotificationToken();
        String message = makeMessage(targetToken, title, body, data);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message,
                MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL + "messages:send")
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response = client.newCall(request).execute();

        response.close();
    }

    public void sendNearByHelper(WorkDto workDto) throws FirebaseMessagingException{
        Work work = workRepository.findById(workDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));

        String body = work.getTitle();

        List<User> aroundHelperList = new ArrayList<>(locationService.getAroundHelperList(new Location(workDto.getLatitude(), workDto.getLongitude())));
        for (User user : aroundHelperList)
            if(!user.getNotification())
                aroundHelperList.remove(user);

        aroundHelperList.remove(work.getCustomer());

        sendMessageGroup(aroundHelperList, notificationTitle, body);
        alarmService.sendGroup(aroundHelperList, notificationTitle, body);
    }


    public String sendGroupTest(List<User> userList, String title, String body) throws FirebaseMessagingException, IOException {
        List<String> strings = new ArrayList<>();
        for (User user : userList) {
            strings.add(user.getNotificationToken());
        }


        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .addAllTokens(strings)
                .build();
        BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);

        if (response.getFailureCount() > 0) {
            List<SendResponse> responses = response.getResponses();
            List<String> failedTokens = new ArrayList<>();
            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    failedTokens.add(strings.get(i));
                }
            }

            log.info("List of tokens that caused failures: " + failedTokens);
        }
        return "ok";
    }



    public void sendMessageGroup(List<User> userList, String title, String body) throws FirebaseMessagingException {
        if(userList.size() == 0)
            return;

        List<String> strings = new ArrayList<>();
        for (User user : userList) {
            if(user.isAccountNonLocked() == true)
            strings.add(user.getNotificationToken());
        }

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putData("routeType", RouteOptions.NEARBY_WORK_VIEW.getDetail())
                .addAllTokens(strings)
                .build();
        BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
        if (response.getFailureCount() > 0) {
            List<SendResponse> responses = response.getResponses();
            List<String> failedTokens = new ArrayList<>();
            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    failedTokens.add(strings.get(i));
                }
            }
            log.info("List of tokens that caused failures: " + failedTokens);
        }
    }

    public void chatNotification(String conversationId) throws IOException {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(()-> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        if(conversation.getChatList().size() == 0)
            return;

        User findUser;
        Chat chat = conversation.getChatList().get(conversation.getChatList().size() - 1);

        if(!conversation.getCreatorName().equals(chat.getSenderName()))
            findUser = userRepository.findById(conversation.getCreatorId()).get();

        else findUser = userRepository.findById(conversation.getParticipantId()).get();
        FcmMessage.Data data = FcmMessage.Data.builder().routeType(RouteOptions.CONVERSATION_VIEW.getDetail()).routeData(conversationId).build();

        sendMessageTo(findUser, notificationTitle, NotificationBody.CHAT.getDetail(), data);
    }

    public void sendWorkProceeding(Work work, User user) throws IOException {
        String body = "";
        FcmMessage.Data data;
        Conversation conversation = conversationRepository.findByWorkId(work.getId()).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        if(work.getProceedingStatus().equals(WorkProceedingStatus.STARTED)){
            body = NotificationBody.STARTED_WORK.getDetail();
            data = FcmMessage.Data.builder().routeType(RouteOptions.CONVERSATION_VIEW.getDetail()).routeData(conversation.get_id()).build();
        } else if(work.getProceedingStatus().equals(WorkProceedingStatus.FINISHED)){
            body = NotificationBody.FINISHED_WORK.getDetail();
            data = FcmMessage.Data.builder().routeType(RouteOptions.CONVERSATION_VIEW.getDetail()).routeData(conversation.get_id()).build();
        } else if(work.getProceedingStatus().equals(WorkProceedingStatus.REWARDED)){
            body = NotificationBody.REWARDED_WORK.getDetail();
            data = FcmMessage.Data.builder().routeType(RouteOptions.FINISH_WORK_VIEW.getDetail()).routeData(conversation.get_id()).build();
        }
        else return;

        sendMessageTo(user, notificationTitle, body, data);
        alarmService.send(user, notificationTitle, body, data.getRouteType());
    }
    public void sendReviewUpload(Review review) throws IOException {

        FcmMessage.Data data = FcmMessage.Data.builder().routeType(RouteOptions.MY_REVIEW_VIEW.getDetail()).build();
        sendMessageTo(review.getUser(), notificationTitle, NotificationBody.REVIEW_UPLOADED.getDetail(), data);
        alarmService.send(review.getUser(), notificationTitle, NotificationBody.REVIEW_UPLOADED.getDetail(), data.getRouteType());
    }

    public void sendReportExecuted(Report report) throws IOException {

        FcmMessage.Data data = FcmMessage.Data.builder().routeType(RouteOptions.REPORT_VIEW.getDetail()).build();
        sendMessageTo(report.getReportUser(), notificationTitle, NotificationBody.EXECUTED_REPORT.getDetail(), data);
        alarmService.send(report.getReportUser(), notificationTitle, NotificationBody.EXECUTED_REPORT.getDetail(), data.getRouteType());
    }

    public void workDeleteNotification(Work work) throws IOException {
        FcmMessage.Data data = FcmMessage.Data.builder().routeType(RouteOptions.MAIN_VIEW.getDetail()).build();
        sendMessageTo(work.getCustomer(), notificationTitle, NotificationBody.WORK_DELETED.getDetail(), data);
        alarmService.send(work.getCustomer(), notificationTitle, NotificationBody.WORK_DELETED.getDetail(), data.getRouteType());
    }

    public void nonFinishedWorkDeleteNotification(Work work) throws IOException {
        FcmMessage.Data data = FcmMessage.Data.builder().routeType(RouteOptions.MAIN_VIEW.getDetail()).build();
        sendMessageTo(work.getCustomer(), notificationTitle, NotificationBody.NON_FINISHED_WORK_DELETED.getDetail(), data);
        sendMessageTo(work.getHelper(), notificationTitle, NotificationBody.NON_FINISHED_WORK_DELETED.getDetail(), data);
        alarmService.send(work.getHelper(), notificationTitle, NotificationBody.NON_FINISHED_WORK_DELETED.getDetail(), data.getRouteType());
        alarmService.send(work.getCustomer(), notificationTitle, NotificationBody.NON_FINISHED_WORK_DELETED.getDetail(), data.getRouteType());
    }

    private String makeMessage(String targetToken, String title, String body, FcmMessage.Data data) throws JsonProcessingException {
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .image(null)
                                .build()
                        ).data(data).build()).validateOnly(false).build();
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