package com.hedgehog.client.basket.controller;


import com.hedgehog.client.auth.model.dto.LoginDetails;
import com.hedgehog.client.auth.model.dto.LoginUserDTO;
import com.hedgehog.client.basket.model.dto.CartSelectDTO;
import com.hedgehog.client.basket.model.dto.CartSumDTO;
import com.hedgehog.client.basket.model.service.BasketService;
import com.hedgehog.client.basket.model.service.BasketServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;

@Controller
@RequestMapping("/basket")
public class BasketController {


    private final BasketService basketService;//서비스 주입받아서 사용
    //서비스 계층에서ㅓ 정의된 메소드 호출해서 비즈니스로직을 수행

    @Autowired
    public BasketController(BasketService basketService) {
        this.basketService = basketService;
        //의존성 주입
    }

    @GetMapping("/cart")
    public ModelAndView selectCartList(@AuthenticationPrincipal LoginDetails loginDetails, ModelAndView mv) {

        LoginUserDTO loginUserDTO = loginDetails.getLoginUserDTO();
        System.out.println(loginDetails.getLoginUserDTO().toString());
        mv.addObject("userCode",  loginUserDTO.getUserCode());

        List<CartSelectDTO> cartItemList =basketService.selectCartList( loginUserDTO.getUserCode());
        mv.addObject("cartItemList", cartItemList);


        List<CartSumDTO> cartSumList = basketService.selectCartSum();
        mv.addObject("cartSumList" , cartSumList);

        int totalSum = basketService.getTotalCartSum(cartSumList);
        mv.addObject("totalSum", totalSum);

        mv.setViewName("client/content/basket/cart");
        return mv;
    }


    @PostMapping(value="/cart/delete", produces = "text/plain; charset=UTF-8")
    @ResponseBody
    public String deleteSelectedItems(
            @RequestBody List<Integer> cartCodes) {
        basketService.deleteCartItems(cartCodes);

        return "success";
    }




}
