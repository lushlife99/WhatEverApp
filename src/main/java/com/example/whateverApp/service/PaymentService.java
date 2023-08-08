package com.example.whateverApp.service;

import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.repository.WorkRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final WorkRepository workRepository;
    private final JwtTokenProvider jwtTokenProvider;
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
     * midde -> Customer
     *
     */

    @Transactional
    public void beforeWork(WorkDto workDto, HttpServletRequest request){
        User customer = userRepository.findById(workDto.getCustomerId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(!jwtTokenProvider.getUser(request).equals(customer))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        if(customer.getReward().compareTo(workDto.getReward()) < 0)
            throw new CustomException(ErrorCode.LACK_REWORD);

        customer.setReward(customer.getReward() - workDto.getReward());
        userRepository.save(customer);
    }

    @Transactional
    public void afterWork(WorkDto workDto, HttpServletRequest request){
        User helper = userRepository.findById(workDto.getHelperId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        User customer = userRepository.findById(workDto.getCustomerId())
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(!user.equals(customer))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        helper.setReward(helper.getReward() + workDto.getReward());
        userRepository.save(helper);
    }

    @Transactional
    public void chargeReward(HttpServletRequest request) {
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        user.setReward(user.getReward());

    }
}
