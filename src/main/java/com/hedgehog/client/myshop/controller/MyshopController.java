package com.hedgehog.client.myshop.controller;

import com.hedgehog.client.auth.model.dto.LoginDetails;
import com.hedgehog.client.auth.model.dto.LoginUserDTO;
import com.hedgehog.client.auth.model.dto.MemberDTO;
import com.hedgehog.client.auth.model.service.AuthServiceImpl;
import com.hedgehog.client.myshop.model.dto.ModifyForm;
import com.hedgehog.client.myshop.model.service.MyshopService;
import com.hedgehog.common.common.exception.UserCertifiedException;
import com.hedgehog.common.common.exception.UserEmailNotFoundException;
import com.hedgehog.common.logout.SessionLogout;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
@RequestMapping("/myshop")
public class MyshopController {
    private final MyshopService myshopService;
    private final AuthServiceImpl authService;
    private final PasswordEncoder passwordEncoder;

    public MyshopController(MyshopService myshopService, AuthServiceImpl authService, PasswordEncoder passwordEncoder) {
        this.myshopService = myshopService;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("mypage")
    public ModelAndView mypage(@AuthenticationPrincipal LoginDetails loginDetails, ModelAndView mv) {
        LoginUserDTO loginUserDTO = loginDetails.getLoginUserDTO();
        mv.addObject("name", loginUserDTO.getName());

        int point = myshopService.getMyPoint(loginUserDTO.getUserCode());
        mv.addObject("point", point);
        mv.addObject("userId", loginUserDTO.getUserId());
        mv.setViewName("/client/content/myshop/mypage");
        return mv;
    }

    @GetMapping("info")
    public ModelAndView memberInfo(@AuthenticationPrincipal LoginDetails loginDetails, ModelAndView mv) {
        LoginUserDTO loginUserDTO = loginDetails.getLoginUserDTO();
        MemberDTO member = myshopService.getMemberInfo(loginUserDTO.getUserCode());
        mv.addObject("name", loginUserDTO.getName());
        mv.addObject("birthday", member.getBirthday());
        mv.addObject("gender", member.getGender());
        mv.addObject("userId", loginUserDTO.getUserId());
        mv.addObject("email", member.getEmail());
        mv.addObject("emailService", member.getEmailService());
        mv.addObject("phone", member.getPhone());
        mv.setViewName("/client/content/myshop/info");
        return mv;
    }

    @GetMapping("/modifyInfo")
    public ModelAndView modifyInfoPage(@AuthenticationPrincipal LoginDetails loginDetails, ModelAndView mv) {
        LoginUserDTO loginUserDTO = loginDetails.getLoginUserDTO();
        MemberDTO member = myshopService.getMemberInfo(loginUserDTO.getUserCode());
        mv.addObject("name", loginUserDTO.getName());
        mv.addObject("birthday", member.getBirthday());
        mv.addObject("gender", member.getGender());
        mv.addObject("userId", loginUserDTO.getUserId());
        mv.addObject("email", member.getEmail());
        mv.addObject("emailService", member.getEmailService());
        mv.addObject("phone", member.getPhone());
        mv.setViewName("/client/content/myshop/modifyInfo");
        return mv;
    }

    @PostMapping(value = "/checkEmail", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object> checkEmail(@AuthenticationPrincipal LoginDetails loginDetails,
                                          @RequestParam String email) throws UserEmailNotFoundException, UserCertifiedException, MessagingException, UnsupportedEncodingException {
        Map<String, Object> response = new HashMap<>();
        LoginUserDTO loginUserDTO = loginDetails.getLoginUserDTO();
        int userCode = loginUserDTO.getUserCode();
        String userEmail = myshopService.getEmail(userCode); // 일단 이메일 가져오기.
        System.out.println(userEmail);
        System.out.println(email);
        if (userEmail.equals(email)) {
            response.put("result", "sameEmail");
        } else {
            boolean isEmailExist = authService.selectMemberByEmail(email); // 이메일이 있는지.
            if (!isEmailExist) {
                int min = 100000;
                int max = 1000000;
                String randomCode = String.valueOf(new Random().nextInt(max - min) + min);
                int certifiedCode = authService.selectCertifiedNumber(randomCode);
                System.out.println(certifiedCode);
                boolean isEmailSend = authService.sendCheckEmailMail(email, randomCode);

                if (!isEmailSend) {
                    response.put("result", "sendMiss");
                }
                response.put("certifiedCode", certifiedCode);
            }
            response.put("result", isEmailExist ? "fail" : "success");
        }
        return response;
    }

    @PostMapping(value = "/emailCertify", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object> checkEmail(@RequestParam int inputCertifiedCode,
                                          @RequestParam String certifiedKey) {
        Map<String, Object> response = new HashMap<>();
        boolean isDuplicated = authService.certifyEmail(inputCertifiedCode, certifiedKey); // Member 부분에서

        response.put("result", isDuplicated ? "fail" : "success");
        return response;
    }

    @PostMapping("/modifyInfo")
    public String modifyInfo(@AuthenticationPrincipal LoginDetails loginDetails,
                             @ModelAttribute ModifyForm modifyForm,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest req,
                             HttpServletResponse res) {
        MemberDTO member = new MemberDTO(
                modifyForm.getUserId(),
                passwordEncoder.encode(modifyForm.getUserPwd()),
                modifyForm.getName(),
                modifyForm.getEmail(),
                modifyForm.getPhone(),
                modifyForm.getBirthday(),
                modifyForm.getGender(),
                modifyForm.getHiddenCertifiedKey(),
                modifyForm.getEmailService());

        boolean modifySuccess = myshopService.modifyMember(loginDetails.getLoginUserDTO().getUserCode(), member);

        if (modifySuccess) {
            redirectAttributes.addFlashAttribute("message", "회원정보 변경이 완료되었습니다.");
            SessionLogout.invalidSession(req, res);
        } else {
            redirectAttributes.addFlashAttribute("message", "알 수 없는 오류로 회원정보변경에 실패했습니다.");
        }
        return "redirect:/";
    }

    @GetMapping("/guestOrderSearch")
    public String guestOrderSearch() {
        return "/client/content/myshop/guestOrderSearch";
    }
}
