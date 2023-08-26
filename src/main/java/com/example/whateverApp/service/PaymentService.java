package com.example.whateverApp.service;

import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.model.entity.PaymentsInfo;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.jpaRepository.PaymentRepository;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    private final WorkServiceImpl workService;

    private final UserServiceImpl userService;


    /**
     * 은행이름에 따른 코드들을 반환해줌<br>
     * KG이니시스 기준.
     */
    public String code(String bankName) {
        String code="";
        if(bankName.equals("우리은행")||bankName.equals("우리")) code="20";
        else if(bankName.equals("국민은행")||bankName.equals("국민")) code="04";
        return code;
    }

    /**
     * 현재 결제번호에 해당하는 정보를 갖고와서 반환해줌.
     * @param paymentsNo
     * @return
     */

    @Transactional
    public PaymentsInfo paymentLookupService(long paymentsNo) {
        PaymentsInfo paymentsInfo = paymentRepository.getById(paymentsNo);
        return paymentsInfo;
    }

    /**
     * 아임포트 서버쪽 결제내역과 DB에 물건가격을 비교하는 서비스. <br>
     * 다름 -> 예외 발생시키고 GlobalExceptionHandler쪽에서 예외처리 <br>
     * 같음 -> 결제정보를 DB에 저장(PaymentsInfo 테이블)
     * @param irsp (아임포트쪽 결제 내역 조회 정보)
     */
    @Transactional
    public void verifyIamportService(IamportResponse<Payment> irsp, int amount, long workId) throws CustomException {
        Work work = workService.get(workId);

        if(irsp.getResponse().getAmount().intValue()!=amount || amount != work.getReward())
            throw new CustomException(ErrorCode.UNVERIFIED_REWARD_AMOUNT);

        /**
         *
         * BuyerName에 userId(Long)가 들어가야함.
         */
        //아임포트에서 서버쪽 결제내역과 DB의 결제 내역 금액이 같으면 DB에 결제 정보를 삽입.
        User user = userService.get(Long.parseLong(irsp.getResponse().getBuyerName()));

        PaymentsInfo paymentsInfo = PaymentsInfo.builder()
                .payMethod(irsp.getResponse().getPayMethod())
                .impUid(irsp.getResponse().getImpUid())
                .merchantUid(irsp.getResponse().getMerchantUid())
                .amount(irsp.getResponse().getAmount().intValue())
                .buyerAddr(irsp.getResponse().getBuyerAddr())
                .buyerPostcode(irsp.getResponse().getBuyerPostcode())
                .user(user)
                .work(work)
                .build();

        paymentRepository.save(paymentsInfo);
    }

//
//    @Transactional
//    public CancelData cancelData(Map<String,String> map,
//                                 IamportResponse<Payment> lookUp,
//                                 PrincipalDetail principal, String code) throws CustomException {
//        //아임포트 서버에서 조회된 결제금액 != 환불(취소)될 금액 이면 예외발생
//        if(lookUp.getResponse().getAmount()!=new BigDecimal(map.get("checksum")))
//            throw new CustomException();
//
//        CancelData data = new CancelData(lookUp.getResponse().getImpUid(),true);
//        data.setReason(map.get("reason"));
//        data.setChecksum(new BigDecimal(map.get("checksum")));
//        data.setRefund_holder(map.get("refundHolder"));
//        data.setRefund_bank(code);
//        data.setRefund_account(principal.getBankName());
//        return data;
//    }


}