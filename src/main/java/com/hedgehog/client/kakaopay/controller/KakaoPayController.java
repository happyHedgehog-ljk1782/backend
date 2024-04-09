package com.hedgehog.client.kakaopay.controller;

import com.hedgehog.client.auth.model.dto.LoginDetails;
import com.hedgehog.client.auth.model.dto.LoginUserDTO;
import com.hedgehog.client.kakaopay.model.dto.ApproveResponse;
import com.hedgehog.client.kakaopay.model.dto.ReadyResponse;
import com.hedgehog.client.kakaopay.model.dto.OrderPayment;
import com.hedgehog.client.kakaopay.model.service.KakaoPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.print.attribute.standard.PrinterURI;
import java.util.List;


@Controller
@SessionAttributes({"tid", "OrderPayment"})
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;
    private final HttpServletRequest request;

    public KakaoPayController(KakaoPayService kakaoPayService, HttpServletRequest request) {
        this.kakaoPayService = kakaoPayService;
        this.request = request;
    }


    @PostMapping("/kakao/pay")
    public @ResponseBody ReadyResponse payReady(
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam(required = false) int savedPoint,
            @RequestParam(required = false) int originalTotalOrder,
            @RequestParam(required = false) int deliveryPrice,
            @RequestParam int AllOriginalTotalOrder,
            @RequestParam(required = false) int usingPoint,
            @RequestParam(required = false) String deliveryName,
            @RequestParam String deliveryPhone,
            @RequestParam String deliveryRequest,
            @RequestParam List<Integer> productCode,
            @RequestParam List<Integer> count,
            @AuthenticationPrincipal LoginDetails loginDetails,
            OrderPayment orderpayment,


            Model model) {

        LoginUserDTO loginUserDTO = loginDetails.getLoginUserDTO();
        orderpayment.setFinalPrice(AllOriginalTotalOrder);

        ReadyResponse readyResponse = kakaoPayService.payReady(name, phone, email,
                savedPoint, originalTotalOrder, deliveryPrice
                , AllOriginalTotalOrder, usingPoint, deliveryName, deliveryPhone, deliveryRequest, loginDetails, productCode
                , count);

        orderpayment.setProductCode(productCode);
        orderpayment.setPointUsage(usingPoint);
        orderpayment.setUserCode(loginUserDTO.getUserCode());
        orderpayment.setDeliveryCharge(deliveryPrice);
        orderpayment.setFinalPrice(AllOriginalTotalOrder);
        orderpayment.setCount(count);
        orderpayment.setPointCharge(savedPoint + usingPoint);
        orderpayment.setCostPrice(originalTotalOrder);
        orderpayment.setReducedPrice(usingPoint);

        model.addAttribute("approval_url", readyResponse.getNext_redirect_pc_url());
        model.addAttribute("productCode", orderpayment.getProductCode());
        model.addAttribute("tid", readyResponse.getTid());
        model.addAttribute("userCode", orderpayment.getUserCode());
        model.addAttribute("AllOriginalTotalOrder", AllOriginalTotalOrder);
        model.addAttribute("productCode", orderpayment.getProductCode());
        model.addAttribute("OrderPayment", orderpayment);
        model.addAttribute("pointUsage", usingPoint);

        return readyResponse;
    }


    @Transactional
    @GetMapping("/kakao/pay/complete")
    public String payCompleted(
            @RequestParam("pg_token") String pgToken,
            @ModelAttribute("tid") String tid,
            @ModelAttribute("OrderPayment") OrderPayment orderpayment,
            Model model,
            RedirectAttributes redirectAttributes) {

        model.addAttribute("tid", tid);
        model.addAttribute("pg_Token", pgToken);
        model.addAttribute("orderPayment", orderpayment);
        model.addAttribute("usedPoint", orderpayment.getPointUsage());
        model.addAttribute("productCode", orderpayment.getProductCode());
        ApproveResponse approveResponse = kakaoPayService.payApprove(tid, pgToken);

        kakaoPayService.saveOrderDetail(orderpayment.getUserCode(), orderpayment);

        redirectAttributes.addFlashAttribute("OrderPayment", orderpayment);

        return "redirect:/clientOrder/orderCompleted";
    }
}


