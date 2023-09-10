package com.example.whateverApp.service;

import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.WorkProceedingStatus;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.CharConversionException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class RewardService {

    private final UserRepository userRepository;
    private final WorkRepository workRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final String BANK_API_URL = "https://developers.nonghyup.com/ReceivedTransferAccountNumber.nh";
    private final String accessToken = "5dd9ada786f5df42a9459ab1220c8d46ee377f402f78036d630854146f9b6b8b";
    private final String FinAcno = "00820100019960000000000015422";
    private final String iscd = "001996";
    private int isTuno = 300;
    /**
     * chan
     *
     * Customer -> middle -> Helper
     * 일이 끝나고 받을 때 고객에게 reward가 없으면 상황, 코드상 많이 복잡해져버림.
     * 그래서 middle은 일이 시작되기 전 reward를 받아놓는 단계라고 보면 된다.
     *
     * BeforeWork : Customer -> middle
     * AfterWork : middle -> helper
     *
     * 만약 일이 제대로 안끝났다면..? -> 2가지 상황이 있다.
     * 1. 후처리
     * 고객이 후에 컴플레인을 걸어서 돈을 다시 징수해 와야 하는 경우
     * Helper -> Customer
     *
     * 만약 Helper에게 reward가 없다면??
     * 먼저 reward를 충전하라고 알람을 보내야함.
     * 그리고 어느정도 기간을 준 뒤, reward를 되돌려 줄 수 없을 때 아이디를 정지시켜야함.
     * 근데 이것도 로직이 애매해짐.
     * 1. reward는 똑같이 돌려받지 못함. 아이디를 정지시키는 것은 처벌일 뿐임.
     * 2. 아이디를 정지시키려면, 같은 실명으로 인증된 다른 계정까지 알고 막아야 한다. -> 애초에 회원가입 할 때 실명인증을 해야된다는 것.
     *
     * 그냥 애초에 Customer에서 심부름이 끝난 것을 확인한 후에는 reward가 전송되고, 후에 이를 돌려받을 수 없게 만들어놓는것이 베스트인듯.
     * 그렇다면 처음에 심부름 결제 확인버튼을 누를 때 꼭 안내문에 뜨게 해야됨.
     *
     * 결론 : 후처리는 안하는걸로.
     *
     * 2. 진행되는 도중에 stop
     * 제대로 일이 종료가 안된 경우. 그럼 다시 돌려놔야함.
     * middle -> Customer
     *
     */

    @Transactional
    public void beforeWork(WorkDto workDto, HttpServletRequest request){
        User customer = userRepository.findById(workDto.getCustomerId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(!jwtTokenProvider.getUser(request).equals(customer) || workDto.getWorkProceedingStatus() != WorkProceedingStatus.CREATED.ordinal())
            throw new CustomException(ErrorCode.BAD_REQUEST);

        if(customer.getReward().compareTo(workDto.getReward()) < 0)
            throw new CustomException(ErrorCode.LACK_REWORD);

        customer.setReward(customer.getReward() - workDto.getReward());
    }

    @Transactional
    public void afterWork(Work work){
        work.getHelper().setReward(work.getHelper().getReward() + work.getReward());
    }

    @Transactional
    public void chargeRewardToCustomer(Work work, HttpServletRequest request) {
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(!work.getCustomer().getId().equals(user.getId()))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        user.setReward(work.getReward() + user.getReward());
    }

    public void chargeRewardToCustomer(Work work) {

        User customer = work.getCustomer();
        customer.setReward(work.getReward() + customer.getReward());
    }
    /**
     * 230827 chan
     * transfer method
     * 금율결제원의 오픈뱅킹 api를 지금 우리 상황에서 쓰려면 거의 불가능에 가까움.
     * 오픈뱅킹 api를 사용하려면 보안점검을 받아야하고, 받는데만 초기비용이 엄청나게 많이 듦.(천만원 이상)
     * 테스트용 url은 금융결제원에서 제공하긴 하지만, 애초에 AccessToken을 발급받지 못함.
     * 그래서 일단 일단 테스트 용도로 농협 api를 이용해서 reward를 출금하는 함수를 만들어놓음.
     *
     * 하지만 이 함수가 동작하려면 몇가지 제한사항이 있음.
     * 1. 고객의 계좌가 농협api에서 등록한 테스트용 가상계좌여야 함.
     * 2. 고객 계좌의 존재 여부를 파악하지 못함. 정확히는 농협api에서 계좌조회 서비스를 제공하고 있긴 하지만. 이는 테스트용일 뿐이고 배포하려면 똑같이 보안점검을 받아야 하기 때문에 초기비용이 많이 듦.
     *
     * 제한사항 1,2를 해결할 수 없음.
     *
     * 결론적으로 이 함수의 제한사항.
     * 위에서 설명한 문제 때문에 고객의 계좌를 농협에서 등록한 테스트용 가상계좌로 고정해놔야 한다.
     * 그래서 requestBody에 추가되는 Bncd -> 이건 은행 식별번호인데 농협을 가르키는 011로 고정한다.
     * 고객의 계좌를 농협api 테스트용 가상계좌로 고정한다.
     *
     * 나중에 제한사항이 해결된다면 user Entity에 있는 생년월일 정보를 바탕으로 유저의 계좌를 조회해서 송금할 수 있는듯.
     * 일단 이렇게 송금 테스트함수는 마무리.
     * @param request
     */
    @Transactional
    public UserDto transfer(int amount, HttpServletRequest request) throws IOException {
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(amount > user.getReward())
            throw new CustomException(ErrorCode.AMOUNT_IS_MORE_THAN_REWARD);

        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("ApiNm", "ReceivedTransferAccountNumber");
        jsonObject2.put("FintechApsno", "001");
        jsonObject2.put("Tsymd", LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        jsonObject2.put("Trtm", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        jsonObject2.put("Iscd", iscd);
        jsonObject2.put("ApiSvcCd", "DrawingTransferA");
        jsonObject2.put("IsTuno", Integer.toString(isTuno++));
        jsonObject2.put("AccessToken", accessToken);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Header", jsonObject2);
        jsonObject.put("Bncd", "011");
        jsonObject.put("Acno", user.getBankAccount());
        jsonObject.put("Tram", amount);
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));
        log.info(jsonObject.toString());
        Request getFinNoRequest = new Request.Builder()
                .url(BANK_API_URL)
                .post(requestBody)
                .build();

        log.info(getFinNoRequest.toString());
        Response response = client.newCall(getFinNoRequest).execute();
        String string = response.body().string();

        if(string.contains("정상처리 되었습니다")){
            user.setReward(user.getReward() - amount);
        }
        else throw new CustomException(ErrorCode.TRANSFER_ERROR);

        log.info(string);
        return new UserDto(user);
    }


}
