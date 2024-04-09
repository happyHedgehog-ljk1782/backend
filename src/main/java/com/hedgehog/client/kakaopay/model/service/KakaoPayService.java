package com.hedgehog.client.kakaopay.model.service;

import com.hedgehog.client.auth.model.dto.LoginDetails;
import com.hedgehog.client.auth.model.dto.LoginUserDTO;
import com.hedgehog.client.basket.model.dto.CartSelectDTO;
import com.hedgehog.client.kakaopay.model.dao.KakaoPayMapper;
import com.hedgehog.client.kakaopay.model.dto.OrderPayment;
import com.hedgehog.client.kakaopay.model.dto.ReadyResponse;
import com.hedgehog.client.kakaopay.model.dto.ApproveResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@SessionAttributes({"tid", "orderPayment"})
public class KakaoPayService {
    private final KakaoPayMapper kakaoMapper;

    private static final String HOST = "https://kapi.kakao.com";

    public KakaoPayService(KakaoPayMapper kakaoMapper) {
        this.kakaoMapper = kakaoMapper;
    }

    ReadyResponse readyResponse;

    public ReadyResponse payReady(String name, String phone, String email,
                                  int savedPoint, int originalTotalOrder,
                                  int deliveryPrice, int AllOriginalTotalOrder,
                                  int usingPoint, String deliveryName, String deliveryPhone,
                                  String deliveryRequest, LoginDetails loginDetails,
                                  List<Integer> productCode, List<Integer> count) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();

        String orderId = "100";

        parameters.add("cid", "TC0ONETIME");
        parameters.add("partner_order_id", "4");
        parameters.add("partner_user_id", "1in가구");
        parameters.add("item_name", "스틸다 벙커침대 싱글");
        parameters.add("quantity", "2");
        parameters.add("total_amount", String.valueOf(AllOriginalTotalOrder));
        parameters.add("tax_free_amount", "0");
        parameters.add("approval_url", "http://localhost:8080/kakao/pay/complete");
        parameters.add("cancel_url", "http://localhost:8080/clientOrder/orderFailed");
        parameters.add("fail_url", "http://localhost:8080/clientOrder/orderFailed");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, this.getHeaders());

        RestTemplate template = new RestTemplate();

        String url = "https://kapi.kakao.com/v1/payment/ready";

        ReadyResponse readyResponse = template.postForObject(url, requestEntity, ReadyResponse.class);

        return readyResponse;
    }

    public ApproveResponse payApprove(String tid, String pgToken){
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();

        parameters.add("cid", "TC0ONETIME");
        parameters.add("tid", tid);
        parameters.add("partner_order_id", "4"); // 주문아이ㅅ
        parameters.add("partner_user_id", "1in가구");
        parameters.add("pg_token", pgToken);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, this.getHeaders());

        RestTemplate template = new RestTemplate();
        String url = "https://kapi.kakao.com/v1/payment/approve";

        ApproveResponse approveResponse = template.postForObject(url, requestEntity, ApproveResponse.class);

        return approveResponse;
    }

    private HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + "c84521fef561a0b8f63c5438a75390a6");
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        return headers;
    }


    public void saveOrderDetail (int userCode, OrderPayment orderpayment){
        orderpayment.setUserCode(userCode);

        kakaoMapper.saveOrderDetail(userCode, orderpayment);
    }
}
