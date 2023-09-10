package com.example.whateverApp.controller;

import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.model.entity.PaymentsInfo;
import com.example.whateverApp.service.PaymentService;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class PaymentController {
    private final IamportClient iamportClientApi;
    @Autowired private PaymentService paymentService;

    //생성자로 rest api key와 secret을 입력해서 토큰 바로생성. -> 나중에 효민이한테 물어봐야함.
    public PaymentController() {
        this.iamportClientApi = new IamportClient("6796671545054859",
                "064a5442d844755e7f75228e97c52f81a82e80bd67136a309ba026caa2165e21bbf44deb0b6b0638");
    }

    public IamportResponse<Payment> paymentLookup(String impUid) throws IamportResponseException, IOException {
        return iamportClientApi.paymentByImpUid(impUid);
    }

    @PostMapping("verifyIamport")
    public IamportResponse<Payment> verifyIamport(@RequestBody Map<String,String> map) throws IamportResponseException, IOException, CustomException {
        String impUid = map.get("imp_uid");//실제 결제금액 조회위한 아임포트 서버쪽에서 id
        long workId = Long.parseLong(map.get("workId")); //DB에서 물건 가격 조회를 위한 번호
        int amount = Integer.parseInt(map.get("amount"));//실제로 유저가 결제한 금액

        //아임포트 서버쪽에 결제된 정보 조회.
        //paymentByImpUid 는 아임포트에 제공해주는 api인 결제내역 조회(/payments/{imp_uid})의 역할을 함.
        IamportResponse<Payment> irsp = paymentLookup(impUid);

        paymentService.verifyIamportService(irsp, amount, workId);
        return irsp;
    }
}