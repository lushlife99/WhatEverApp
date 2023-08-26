package com.example.whateverApp.service;

import com.example.whateverApp.dto.FcmMessage;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.model.WorkProceedingStatus;
import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.Alarm;
import com.example.whateverApp.model.entity.Review;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
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
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final AlarmService alarmService;
    private final LocationServiceImpl locationService;
    private final WorkRepository workRepository;

    @Value("${file:}")
    private String fileDir;

    public void sendMessageTo(User user, String title, String body) throws IOException {
        String targetToken = user.getNotificationToken();
        String message = makeMessage(targetToken, title, body);

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
        log.info(response.body().string());
    }

    public void sendNearByHelper(WorkDto workDto) throws FirebaseMessagingException, IOException {
        Work work = workRepository.findById(workDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));

        String title = "근처에 새로운 심부름이 등록되었습니다.";
        String body = work.getTitle();

        List<User> aroundHelperList = new ArrayList<>(locationService.getAroundHelperList(new Location(workDto.getLatitude(), workDto.getLongitude())));
        for (User user : aroundHelperList)
            if(!user.getNotification())
                aroundHelperList.remove(user);

        aroundHelperList.remove(work.getCustomer());

        sendMessageGroup(aroundHelperList, title, body);
        alarmService.sendGroup(aroundHelperList, title, body);
    }


    public String sendGroupTest(List<User> userList, String title, String body) throws FirebaseMessagingException, IOException {
        List<String> strings = new ArrayList<>();
        for (User user : userList) {
            strings.add(user.getNotificationToken());
        }
        String to = UUID.randomUUID().toString();

        Base64.Encoder encoder = Base64.getEncoder();
        byte[] photoEncode;
        File file = new File(fileDir + "whateverlogo");
        String s = "";
        if (file.exists()) {
            photoEncode = encoder.encode(new UrlResource("file:" + fileDir + "whateverlogo").getContentAsByteArray());
            s = new String(photoEncode, "UTF8");
        }
        log.info(s);
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).setImage(s).build())
                .addAllTokens(strings)
                .build();
        BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
        System.out.println("success count = " + response.getSuccessCount());
        if (response.getFailureCount() > 0) {
            List<SendResponse> responses = response.getResponses();
            List<String> failedTokens = new ArrayList<>();
            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    // The order of responses corresponds to the order of the registration tokens.
                    failedTokens.add(strings.get(i));
                }
            }

            System.out.println("List of tokens that caused failures: " + failedTokens);
        }
        return "ok";
    }



    public void sendMessageGroup(List<User> userList, String title, String body) throws FirebaseMessagingException {
        if(userList.size() == 0)
            return;

        List<String> strings = new ArrayList<>();
        for (User user : userList) {
            strings.add(user.getNotificationToken());
        }

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .addAllTokens(strings)
                .build();
        BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
        System.out.println("success count = " + response.getSuccessCount());
        if (response.getFailureCount() > 0) {
            List<SendResponse> responses = response.getResponses();
            List<String> failedTokens = new ArrayList<>();
            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    failedTokens.add(strings.get(i));
                }
            }
            System.out.println("List of tokens that caused failures: " + failedTokens);
        }
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

    public void sendWorkProceeding(Work work, User user) throws IOException {
        String title = "";
        String body = "";
        if(work.getProceedingStatus().equals(WorkProceedingStatus.STARTED)){
            title = "심부름이 수락되었습니다. 진행상황을 확인해보세요";
            body = "심부름 정보 : " + work.getTitle();
        } else if(work.getProceedingStatus().equals(WorkProceedingStatus.FINISHED)){
            title = "헬퍼가 심부름을 완료했어요. 심부름 완료 상태를 확인해주세요";
            body = "심부름 정보 : " + work.getTitle();
        } else if(work.getProceedingStatus().equals(WorkProceedingStatus.REWARDED)){
            title = "심부름 검토가 완료되었어요. 심부름비가 전송되었습니다.";
            body = "심부름비 : "+ work.getReward();
        }
        else return;

        sendMessageTo(user, title, body);
        alarmService.send(user, title, body);
    }
    public void sendReviewUpload(Review review) throws IOException {
        String title = "리뷰가 등록되었어요";
        String body = "별점 : "+ review.getRating();

        sendMessageTo(review.getUser(), title, body);
        alarmService.send(review.getUser(), title, body);
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


}