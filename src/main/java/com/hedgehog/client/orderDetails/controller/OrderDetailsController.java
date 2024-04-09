package com.hedgehog.client.orderDetails.controller;

import com.hedgehog.client.auth.model.dto.LoginDetails;
import com.hedgehog.client.auth.model.dto.LoginUserDTO;
import com.hedgehog.client.auth.model.dto.MemberDTO;
import com.hedgehog.client.myshop.model.service.MyshopService;
import com.hedgehog.client.orderDetails.model.dto.*;
import com.hedgehog.client.orderDetails.model.service.OrderDetailsService;
import com.hedgehog.common.common.exception.UserCertifiedException;
import com.hedgehog.common.common.exception.UserEmailNotFoundException;
import com.hedgehog.common.logout.SessionLogout;
import com.hedgehog.common.paging.orderDetailsPaging.OrderDetailsPagenation;
import com.hedgehog.common.paging.orderDetailsPaging.OrderDetailsSelectCriteria;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/myshop")
@AllArgsConstructor
public class OrderDetailsController {
    private final OrderDetailsService orderDetailsService;
    private final MyshopService myshopService;

    @GetMapping("/orderDeliveryInfo")
    public ModelAndView orderDeliveryInfo(@AuthenticationPrincipal LoginDetails loginDetails,
                                          @RequestParam(required = false, defaultValue = "0") String state,
                                          @RequestParam(required = false) LocalDate dateStart,
                                          @RequestParam(required = false) LocalDate dateEnd,
                                          @RequestParam(value = "currentPage", defaultValue = "1") int pageNo,
                                          ModelAndView mv) {
        LoginUserDTO loginUserDTO = loginDetails.getLoginUserDTO();
        int userCode = loginUserDTO.getUserCode();
        if (dateStart == null && dateEnd == null) {
            dateStart = LocalDate.now().minusMonths(3);
            dateEnd = LocalDate.now();
        }
        if (dateEnd == null) {
            dateEnd = LocalDate.now();
        }
        if (dateStart == null) {
            dateStart = LocalDate.of(2000, 1, 1);
        }

        OrderDTO order = new OrderDTO(state, dateStart, dateEnd.plusDays(1));
        String info = "orderDeliveryInfo";
        int totalCount = orderDetailsService.selectTotalCountOrderInfo(userCode, order, info);

        int limit = 5;
        int buttonAmount = 5;

        OrderDetailsSelectCriteria orderDetailsSelectCriteria = OrderDetailsPagenation.getOrderDetailsSelectCriteria(pageNo, totalCount, limit, buttonAmount, order);

        List<OrderListDTO> orderList = orderDetailsService.selectOrderInfoList(userCode, orderDetailsSelectCriteria, info);
        mv.addObject("orderList", orderList);

        orderDetailsSelectCriteria.setOrder(new OrderDTO(state, dateStart, dateEnd));
        mv.addObject("orderDetailsSelectCriteria", orderDetailsSelectCriteria);

        mv.setViewName("/client/content/myshop/orderDeliveryInfo");
        mv.addObject("state", state);
        mv.addObject("dateStart", dateStart);
        mv.addObject("dateEnd", dateEnd);

        /*이부분에서 일주일전, 한달전, 세달전, 여섯달 전에 대한 변수를 반환한다.*/
        mv.addObject("now", LocalDate.now());
        mv.addObject("date7", LocalDate.now().minusDays(7));
        mv.addObject("date30", LocalDate.now().minusMonths(1));
        mv.addObject("date90", LocalDate.now().minusMonths(3));
        mv.addObject("date180", LocalDate.now().minusMonths(6));
        return mv;
    }

    @GetMapping("/exchangePaybackInfo")
    public ModelAndView exchangePaybackInfo(@AuthenticationPrincipal LoginDetails loginDetails,
                                            @RequestParam(required = false, defaultValue = "0") String state,
                                            @RequestParam(required = false) LocalDate dateStart,
                                            @RequestParam(required = false) LocalDate dateEnd,
                                            @RequestParam(value = "currentPage", defaultValue = "1") int pageNo,
                                            ModelAndView mv) {
        LoginUserDTO loginUserDTO = loginDetails.getLoginUserDTO();
        int userCode = loginUserDTO.getUserCode();
        if (dateStart == null && dateEnd == null) {
            dateStart = LocalDate.now().minusMonths(3);
            dateEnd = LocalDate.now();
        }
        if (dateEnd == null) {
            dateEnd = LocalDate.now();
        }
        if (dateStart == null) {
            dateStart = LocalDate.of(2000, 1, 1);
        }

        OrderDTO order = new OrderDTO(state, dateStart, dateEnd.plusDays(1));
        String info = "exchangePaybackInfo";
        int totalCount = orderDetailsService.selectTotalCountOrderInfo(userCode, order, info);

        int limit = 5;
        int buttonAmount = 5;

        OrderDetailsSelectCriteria orderDetailsSelectCriteria = OrderDetailsPagenation.getOrderDetailsSelectCriteria(pageNo, totalCount, limit, buttonAmount, order);
        orderDetailsSelectCriteria.setOrder(new OrderDTO(state, dateStart, dateEnd));

        List<OrderListDTO> orderList = orderDetailsService.selectOrderInfoList(userCode, orderDetailsSelectCriteria, info);
        mv.addObject("orderList", orderList);
        mv.addObject("orderDetailsSelectCriteria", orderDetailsSelectCriteria);

        mv.setViewName("/client/content/myshop/exchangePaybackInfo");
        mv.addObject("state", state);
        mv.addObject("dateStart", dateStart);
        mv.addObject("dateEnd", dateEnd);

        /*이부분에서 일주일전, 한달전, 세달전, 여섯달 전에 대한 변수를 반환한다.*/
        mv.addObject("now", LocalDate.now());
        mv.addObject("date7", LocalDate.now().minusDays(7));
        mv.addObject("date30", LocalDate.now().minusMonths(1));
        mv.addObject("date90", LocalDate.now().minusMonths(3));
        mv.addObject("date180", LocalDate.now().minusMonths(6));
        return mv;
    }

    @PostMapping("/memberOrderDetails")
    @ResponseBody
    public String memberOrderDetails(@AuthenticationPrincipal LoginDetails loginDetails,
                                     @RequestParam int orderCode,
                                     HttpServletRequest req,
                                     HttpServletResponse res) {
        int userCode = loginDetails.getLoginUserDTO().getUserCode();
        boolean result = orderDetailsService.isYourOrder(userCode, orderCode);
        if (!result) {
            SessionLogout.invalidSession(req, res);
        }
        return result == true ? "success" : "fail";
    }

    @GetMapping("/orderDetails")
    public String orderDetailsInfo(@AuthenticationPrincipal LoginDetails loginDetails,
                                   @RequestParam int orderCode,
                                   @RequestParam(required = false) String email,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (loginDetails == null) {
            redirectAttributes.addFlashAttribute("message",
                    "잘못된 접근입니다. 메인으로 돌아갑니다.");
            return "redirect:/";
        }
        LoginUserDTO loginUserDTO = loginDetails.getLoginUserDTO();
        int userCode = loginUserDTO.getUserCode();
        boolean result = orderDetailsService.isYourOrder(userCode, orderCode);
        if (!result) {
            return "redirect:/";
        }
        OrderDetailsCollect orderDetailsCollect = orderDetailsService.getOrderDetails(orderCode);
        model.addAttribute("orderDetails", orderDetailsCollect);
        int sumCostPrice = orderDetailsCollect
                .getOrderDetailsList()
                .stream()
                .collect(Collectors.summingInt((orderDetail) -> orderDetail.getCostPrice() * orderDetail.getCount()));
        model.addAttribute("sumCostPrice", sumCostPrice);
        int sumReducedPrice = orderDetailsCollect
                .getOrderDetailsList()
                .stream()
                .collect(Collectors.summingInt((orderDetail) -> orderDetail.getReducedPrice() * orderDetail.getCount()));
        model.addAttribute("sumReducedPrice", sumReducedPrice);
        int sumDeliveryCharge = orderDetailsCollect
                .getOrderDetailsList()
                .stream()
                .collect(Collectors.summingInt((orderDetail) -> orderDetail.getDeliveryCharge() * orderDetail.getCount()));
        model.addAttribute("sumDeliveryCharge", sumDeliveryCharge);
        int sumPointCharge = orderDetailsCollect
                .getOrderDetailsList()
                .stream()
                .collect(Collectors.summingInt((orderDetail) -> orderDetail.getPointCharge() * orderDetail.getCount()));
        model.addAttribute("sumPointCharge", sumPointCharge);
        model.addAttribute("finalPrice",
                sumCostPrice - sumReducedPrice - orderDetailsCollect.getPointUsage() + sumDeliveryCharge);
        int sumReviewPoint = orderDetailsCollect
                .getOrderDetailsList()
                .stream()
                .collect(Collectors.summingInt((orderDetail) -> orderDetail.getReviewPoint()));
        model.addAttribute("sumReviewPoint", sumReviewPoint);
        MemberDTO member = myshopService.getMemberInfo(userCode);
        model.addAttribute("name", member.getName());
        model.addAttribute("email", member.getEmail());
        model.addAttribute("phone", member.getPhone());
        return "/client/content/myshop/orderDetails";
    }

    @PostMapping("/orderDetails")
    public String guestOrderDetails(@RequestParam(required = false) Integer orderCode,
                                    @RequestParam(required = false) String email,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        if (orderCode == null || email == null) {
            redirectAttributes.addFlashAttribute("message", "주문번호 또는 이메일을 입력해주세요.");
            return "redirect:/myshop/guestOrderSearch";
        }
        Integer newOrderCode = orderDetailsService.selectOrderCode(orderCode, email); // 현재 주문번호에 알맞는 이메일이 있는지. 그리고 같은지
        if (newOrderCode == null) {
            redirectAttributes.addFlashAttribute("message", "조건에 맞는 비회원이 없습니다.\n다시입력해주세요.");
            return "redirect:/myshop/guestOrderSearch";
        }
        if (!newOrderCode.equals(orderCode)) {
            redirectAttributes.addFlashAttribute("message", "계정정보가 달라서 메인으로 돌아갑니다.");
            return "redirect:/";
        }
        OrderDetailsCollect orderDetailsCollect = orderDetailsService.getOrderDetails(orderCode);

        model.addAttribute("orderDetails", orderDetailsCollect);
        int sumCostPrice = orderDetailsCollect
                .getOrderDetailsList()
                .stream()
                .collect(Collectors.summingInt((orderDetail) -> orderDetail.getCostPrice() * orderDetail.getCount()));
        model.addAttribute("sumCostPrice", sumCostPrice);
        int sumReducedPrice = orderDetailsCollect
                .getOrderDetailsList()
                .stream()
                .collect(Collectors.summingInt((orderDetail) -> orderDetail.getReducedPrice() * orderDetail.getCount()));
        model.addAttribute("sumReducedPrice", sumReducedPrice);
        int sumDeliveryCharge = orderDetailsCollect
                .getOrderDetailsList()
                .stream()
                .collect(Collectors.summingInt((orderDetail) -> orderDetail.getDeliveryCharge() * orderDetail.getCount()));
        model.addAttribute("sumDeliveryCharge", sumDeliveryCharge);
        int sumPointCharge = orderDetailsCollect
                .getOrderDetailsList()
                .stream()
                .collect(Collectors.summingInt((orderDetail) -> orderDetail.getPointCharge() * orderDetail.getCount()));
        model.addAttribute("sumPointCharge", sumPointCharge);
        model.addAttribute("finalPrice", sumCostPrice - sumReducedPrice - orderDetailsCollect.getPointUsage() + sumDeliveryCharge);
        int sumReviewPoint = orderDetailsCollect
                .getOrderDetailsList()
                .stream()
                .collect(Collectors.summingInt((orderDetail) -> orderDetail.getReviewPoint()));
        model.addAttribute("sumReviewPoint", sumReviewPoint);

        int userCode = orderDetailsService.selectUserCode(orderCode);
        MemberDTO member = myshopService.getMemberInfo(userCode);
        model.addAttribute("name", member.getName());
        model.addAttribute("email", member.getEmail());
        model.addAttribute("phone", member.getPhone());

        return "/client/content/myshop/orderDetails";
    }

    @PostMapping("/receiveComplete")
    public String receiveOrder(@RequestParam String orderCode, RedirectAttributes redirectAttributes) {
        boolean isReceive = orderDetailsService.updateReceiveOrder(orderCode);
        if (isReceive) {
            redirectAttributes.addFlashAttribute("message", "배송완료 처리에 성공했습니다. \n메인으로 돌아갑니다.");
        } else {
            redirectAttributes.addFlashAttribute("message", "알 수 없는 오류가 발생했습니다. \n메인으로 돌아갑니다.");
        }
        return "redirect:/";
    }
}
