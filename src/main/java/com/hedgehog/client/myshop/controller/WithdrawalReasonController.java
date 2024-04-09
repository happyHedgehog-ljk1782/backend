package com.hedgehog.client.myshop.controller;

import com.hedgehog.client.auth.model.dto.LoginDetails;
import com.hedgehog.client.auth.model.dto.LoginUserDTO;
import com.hedgehog.client.myshop.model.dto.WithdrawalRequestDTO;
import com.hedgehog.client.myshop.model.service.WithdrawalReasonService;
import com.hedgehog.common.logout.SessionLogout;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/myshop/withdrawalReason")
@AllArgsConstructor
public class WithdrawalReasonController {
    private final WithdrawalReasonService withdrawalReasonService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("")
    public String withdrawalReason() {
        return "/client/content/myshop/withdrawalReason";
    }

    @PostMapping("/submit")
    public String submitData(@AuthenticationPrincipal LoginDetails loginDetails,
                             @ModelAttribute WithdrawalRequestDTO withdrawalRequest,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request, HttpServletResponse response) {
        LoginUserDTO loginUserDTO = loginDetails.getLoginUserDTO();

        String inputPwd = withdrawalRequest.getUserPwd();
        String loginUserPwd = loginUserDTO.getUserPwd();
        if (passwordEncoder.matches(inputPwd, loginUserPwd)) {
            // 비밀번호가 일치하는 경우
            LocalDateTime commitDateTime = LocalDateTime.now().with(LocalTime.MIDNIGHT).plusDays(7);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            redirectAttributes.addFlashAttribute("message", "회원탈퇴 신청이 완료되었습니다.\n" +
                    commitDateTime.format(formatter) + " 이전에 들어오시면 탈퇴 신청이 취소됩니다.");

            withdrawalReasonService.insertWithdrawalReason(loginUserDTO.getUserCode(), withdrawalRequest.getSummernoteContent());

            SessionLogout.invalidSession(request, response);

            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("message", "비밀번호가 일치하지 않습니다.");
            return "redirect:/myshop/withdrawalReason";
        }
    }
}
