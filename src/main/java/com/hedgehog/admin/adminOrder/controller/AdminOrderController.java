package com.hedgehog.admin.adminOrder.controller;

import com.hedgehog.admin.adminMember.model.dto.AdminCustomerDTO;
import com.hedgehog.admin.adminOrder.model.dto.AdminOrderDTO;
import com.hedgehog.admin.adminOrder.model.dto.AdminOrderForm;
import com.hedgehog.admin.adminOrder.model.service.AdminOrderServiceImpl;
import com.hedgehog.admin.exception.OrderStateUpdateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/order")
public class AdminOrderController {
    private final AdminOrderServiceImpl adminOrderService;

    public AdminOrderController(AdminOrderServiceImpl adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @PostMapping(value = "/exchange")
    private String exchange(@RequestParam("orderCode") int orderCode,
                            @RequestParam("cause") String cause,
                            Model model,
                            RedirectAttributes rttr) throws OrderStateUpdateException {
        AdminOrderDTO orderDTO = new AdminOrderDTO();
        orderDTO.setOrderCode(orderCode);
        orderDTO.setCause(cause);
        orderDTO.setState("6");

        adminOrderService.exchange(orderDTO);

        AdminOrderDTO orderDetail = adminOrderService.orderDetail(orderCode);
        model.addAttribute("orderDetail", orderDetail);

        rttr.addFlashAttribute("message", "교환신청이 완료되었습니다.");

        return "redirect:/order/orderDetail?orderCode=" + orderCode;
    }

    @PostMapping(value = "/refund")
    private String refund(@RequestParam("orderCode") int orderCode,
                          @RequestParam("cause") String cause,
                          Model model,
                          RedirectAttributes rttr) throws OrderStateUpdateException {
        AdminOrderDTO orderDTO = new AdminOrderDTO();
        orderDTO.setOrderCode(orderCode);
        orderDTO.setCause(cause);
        orderDTO.setState("7");

        adminOrderService.refund(orderDTO);

        AdminOrderDTO orderDetail = adminOrderService.orderDetail(orderCode);
        model.addAttribute("orderDetail", orderDetail);

        rttr.addFlashAttribute("message", "환불신청이 완료되었습니다.");

        return "redirect:/order/orderDetail?orderCode=" + orderCode;
    }

    @GetMapping(value = "/orderDetail")
    private String orderDetail(@RequestParam int orderCode, Model model) {
        AdminOrderDTO orderDetail = adminOrderService.orderDetail(orderCode);

        int finalPrice = 0;
        int finalPrice1 = 0;
        int calPrice = 0;

        for (int i = 0; i < orderDetail.getOrderDetail().size(); i++) {
            int count = orderDetail.getOrderDetail().get(i).getCount();
            int costPrice = orderDetail.getOrderDetail().get(i).getCostPrice();

            finalPrice1 += (count * costPrice);
        }

        finalPrice = finalPrice1 - orderDetail.getPointUsage();

        orderDetail.setSumPrice(finalPrice);

        model.addAttribute("orderDetail", orderDetail);
        return "admin/content/order/orderDetail";

    }

    @PostMapping(value = "/stateUpdate")
    private String orderStateUpdate(@RequestParam("resultCheckbox") List<String> selectedOrderCodes,
                                    @RequestParam("selectCommit") String selectedState,
                                    RedirectAttributes rttr) throws OrderStateUpdateException {
        for (int i = 0; i < selectedOrderCodes.size(); i++) {
            if ("on".equals(selectedOrderCodes.get(i)) || selectedOrderCodes.get(i).isEmpty()) {
                continue;
            } else {
                int orderCode = Integer.parseInt(selectedOrderCodes.get(i));
                AdminOrderDTO orderDTO = new AdminOrderDTO();
                orderDTO.setOrderCode(orderCode);
                orderDTO.setState(selectedState);

                adminOrderService.orderStateUpdate(orderDTO);
            }
        }
        rttr.addFlashAttribute("message", "상태가 변경되었습니다.");
        return "redirect:/order/orderSearch";
    }

    @GetMapping(value = "/orderSearch")
    public ModelAndView orderSearch(@ModelAttribute AdminOrderForm form,
                                    ModelAndView mv) {
        List<AdminOrderDTO> orderSearch = adminOrderService.searchOrderList(form);

        int totalResult = orderSearch.size();

        mv.addObject("orderSearch", orderSearch);
        mv.addObject("totalResult", totalResult);
        mv.setViewName("admin/content/order/order");

        return mv;
    }

    @GetMapping("/orderPage")
    public String order() {
        return "admin/content/order/order.html";
    }
}
