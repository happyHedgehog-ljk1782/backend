package com.hedgehog.client.clientOrder.controller;

import com.hedgehog.client.auth.model.dto.LoginDetails;
import com.hedgehog.client.auth.model.dto.LoginUserDTO;
import com.hedgehog.client.basket.model.dto.CartSelectDTO;
import com.hedgehog.client.clientOrder.model.dto.OrderInfoDTO;
import com.hedgehog.client.clientOrder.model.service.ClientCartServiceImp;
import com.hedgehog.client.kakaopay.model.dto.OrderPayment;
import com.hedgehog.client.kakaopay.model.service.KakaoPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static java.lang.Integer.valueOf;

@Controller
@RequestMapping("/clientOrder")
public class ClientOrderController {

    private final ClientCartServiceImp clientCartService;
    private final KakaoPayService kakaoPayService;

    @Autowired
    public ClientOrderController(ClientCartServiceImp clientCartService, KakaoPayService kakaoPayService) {
        this.clientCartService = clientCartService;
        this.kakaoPayService = kakaoPayService;
    }

    @PostMapping("/cartOrder")
    public ModelAndView clientOrder(@AuthenticationPrincipal LoginDetails loginDetails,
                                    @RequestParam List<Integer> cartcheckbox,
                                    @RequestParam List<Integer> hdAmount,
                                    ModelAndView mv) {

        System.out.println("cartIds = " + cartcheckbox);
        System.out.println("hdAmount = " + hdAmount);

        List<CartSelectDTO> cartList = clientCartService.selectCartOrder(cartcheckbox);
        mv.addObject("cartList", cartList);

        mv.addObject("hdAmountList", hdAmount);
        mv.addObject("cartcheckbox", cartcheckbox);

        int totalSum = calculateTotalSum(cartList, hdAmount);
        mv.addObject("totalSum", totalSum);

        cartOrderInfo(loginDetails, mv);

        mv.setViewName("/client/content/clientOrder/cartOrder");

        return mv;
    }

    private int calculateTotalSum(List<CartSelectDTO> cartList, List<Integer> hdAmountList) {
        int totalSum = 0;
        for (int i = 0; i < cartList.size(); i++) {
            int price = cartList.get(i).getPrice();
            int hdAmount = hdAmountList.get(i);
            totalSum += price * hdAmount;
        }
        return totalSum;
    }

    public void cartOrderInfo(LoginDetails loginDetails,
                              ModelAndView mv) {
        LoginUserDTO loginUserDTO = loginDetails.getLoginUserDTO();
        int point = clientCartService.getOrderPoint(loginUserDTO.getUserCode());
        mv.addObject("name", loginUserDTO.getName());
        mv.addObject("point", point);

        OrderInfoDTO orderInfo = clientCartService.getOrderInfo(loginUserDTO.getUserCode());
        mv.addObject("phone", orderInfo.getPhone());
        mv.addObject("email", orderInfo.getEmail());
    }

    @GetMapping("/orderCompleted") //주문완료 페이지
    public ModelAndView orderCompleted(ModelAndView mv,
                                       @ModelAttribute("OrderPayment") OrderPayment orderpayment,
                                       @AuthenticationPrincipal LoginDetails loginDetails) {
        mv.addObject("finalPrice", orderpayment.getFinalPrice());
        mv.addObject("userCode", loginDetails.getLoginUserDTO().getUserCode());
        mv.addObject("userName", loginDetails.getLoginUserDTO().getName());
        mv.addObject("orderCode", orderpayment.getOrderCode());
        mv.setViewName("/client/content/clientOrder/orderCompleted");
        return mv;
    }

    @GetMapping("/orderFailed")
    public String orderFailed() {
        return "/client/content/clientOrder/orderFailed";
    }
}




