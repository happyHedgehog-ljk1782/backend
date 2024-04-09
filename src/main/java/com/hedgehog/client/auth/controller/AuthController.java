package com.hedgehog.client.auth.controller;

import com.hedgehog.admin.exception.UnregistException;
import com.hedgehog.client.auth.model.dto.MemberDTO;
import com.hedgehog.client.auth.model.dto.RegistrationForm;
import com.hedgehog.client.auth.model.dto.PostDTO;
import com.hedgehog.client.auth.model.service.AuthServiceImpl;
import com.hedgehog.client.auth.model.service.SearchUserInfoService;
import com.hedgehog.common.common.exception.UserCertifiedException;
import com.hedgehog.common.common.exception.UserRegistPostException;
import com.hedgehog.common.logout.SessionLogout;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final PasswordEncoder passwordEncoder;
    private final AuthServiceImpl registService;
    private final SearchUserInfoService searchUserInfoService;

    @GetMapping("/login")
    public ModelAndView loginPage(HttpServletRequest request, ModelAndView mv) {
        Cookie[] cookies = request.getCookies();
        boolean saveIdCookieExists = false;
        String saveId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("userId".equals(cookie.getName())) {
                    saveIdCookieExists = true;
                    saveId = cookie.getValue();
                    break;
                }
            }
        }
        if (saveIdCookieExists) {
            mv.addObject("saveId", saveId);
            mv.addObject("saveIdCheck", true);
        } else {
            mv.addObject("saveId", null);
            mv.addObject("saveIdCheck", false);
        }
        mv.setViewName("client/content/auth/login.html");
        return mv;
    }

    @GetMapping("/regist")
    public ModelAndView registPage(ModelAndView mv) throws UserRegistPostException {
        List<PostDTO> postList = registService.getRegistPosts();
        if (postList.get(0).getPostType().equals("공지사항")) {
            mv.addObject("termsAndConditions", postList.get(0).getContent());
            mv.addObject("privacyPolicy", postList.get(1).getContent());
        } else {
            mv.addObject("termsAndConditions", postList.get(0).getContent());
            mv.addObject("privacyPolicy", postList.get(1).getContent());
        }
        mv.setViewName("/client/content/auth/regist");
        return mv;
    }

    @PostMapping("/regist")
    public String registMember(@ModelAttribute RegistrationForm registrationForm, RedirectAttributes redirectAttributes) throws UnregistException, MessagingException, UnsupportedEncodingException {
        System.out.println(registrationForm);
        MemberDTO newMember = new MemberDTO(
                registrationForm.getUserId(),
                passwordEncoder.encode(registrationForm.getUserPwd()),
                registrationForm.getName(),
                registrationForm.getEmail(),
                registrationForm.getPhone(),
                registrationForm.getBirthday(),
                registrationForm.getGender(),
                registrationForm.getHiddenCertifiedKey(),
                registrationForm.getEmailService());

        boolean registrationSuccess = registService.registMember(newMember);

        if (registrationSuccess) {
            redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다.");
            return "redirect:/auth/login";
        } else {
            redirectAttributes.addFlashAttribute("message", "알 수 없는 오류로 회원가입에 실패했습니다. 메인화면으로 돌아갑니다.");
            return "redirect:/";
        }
    }

    @PostMapping(value = "/checkId", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object> checkId(@RequestParam String userId) {
        Map<String, Object> response = new HashMap<>();
        boolean isDuplicated = registService.selectUserById(userId);
        response.put("result", isDuplicated ? "fail" : "success");
        return response;
    }

    @PostMapping(value = "/checkEmail", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object> checkEmail(@RequestParam String email) throws UserCertifiedException, MessagingException, UnsupportedEncodingException {
        Map<String, Object> response = new HashMap<>();
        boolean isEmailExist = registService.selectMemberByEmail(email); // Member 부분에서
        if (!isEmailExist) {
            int min = 100000;
            int max = 1000000;
            String randomCode = String.valueOf(new Random().nextInt(max - min) + min);
            int certifiedCode = registService.selectCertifiedNumber(randomCode);
            System.out.println(certifiedCode);

            boolean isEmailSend = registService.sendCheckEmailMail(email, randomCode);
            if (!isEmailSend) {
                response.put("result", "sendMiss");
            }

            response.put("certifiedCode", certifiedCode);
        }
        response.put("result", isEmailExist ? "fail" : "success");
        return response;
    }

    @PostMapping(value = "/emailCertify", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object> checkEmail(@RequestParam int inputCertifiedCode,
                                          @RequestParam String certifiedKey) {
        Map<String, Object> response = new HashMap<>();
        boolean isDuplicated = registService.certifyEmail(inputCertifiedCode, certifiedKey); // Member 부분에서

        response.put("result", isDuplicated ? "fail" : "success");
        return response;
    }


    @GetMapping("/searchId")
    public String searchIdPage() {
        return "/client/content/auth/searchId.html";
    }

    @PostMapping(value = "/searchId/checkEmail", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object> searchIdCheckEmail(@RequestParam String email) throws MessagingException, UnsupportedEncodingException {
        Map<String, Object> response = new HashMap<>();
        Integer certificationCode = searchUserInfoService.selectMemberByEmail(email); // Member 부분에서
        if (certificationCode != null) {
            response.put("certifiedCode", certificationCode);
        }
        response.put("result", certificationCode == null ? "fail" : "success");
        return response;
    }

    @PostMapping(value = "/searchId/emailCertify", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object> searchIdCheckEmail(@RequestParam int inputCertifiedCode,
                                                  @RequestParam String certifiedKey) {
        Map<String, Object> response = new HashMap<>();
        boolean isPass = searchUserInfoService.certifyEmail(inputCertifiedCode, certifiedKey); // Member 부분에서

        response.put("result", isPass ? "success" : "fail");
        return response;
    }

    @PostMapping("/searchId")
    public String searchId(@RequestParam String email,
                           @RequestParam int emailAuthenticationNumber,
                           @RequestParam int hiddenCertifiedKey,
                           RedirectAttributes redirectAttributes) {
        String userId = searchUserInfoService.findUserId(email, emailAuthenticationNumber, hiddenCertifiedKey);

        if (userId != null) {
            redirectAttributes.addFlashAttribute("message", "아이디는 " + userId + "입니다.");
            return "redirect:/auth/login";
        } else {
            redirectAttributes.addFlashAttribute("message", "알 수 없는 오류로 아이디 찾기에 실패했습니다. 메인화면으로 돌아갑니다.");
            return "redirect:/";
        }
    }

    @GetMapping("/searchPassword")
    public String searchPasswordPage() {
        return "/client/content/auth/searchPassword.html";
    }

    @PostMapping(value = "/searchPassword/checkEmail", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object> searchPasswordCheckEmail(@RequestParam String userId,
                                                        @RequestParam String email) throws MessagingException, UnsupportedEncodingException {
        Map<String, Object> response = new HashMap<>();
        Integer certificationCode = searchUserInfoService.selectMemberByUserIdAndEmail(userId, email); // Member 부분에서
        if (certificationCode != null) {
            response.put("certifiedCode", certificationCode);
        } else if (certificationCode == -1) {
            response.put("result", "sendMiss");
            return response;
        }
        response.put("result", certificationCode == null ? "fail" : "success");
        return response;
    }

    @PostMapping(value = "/searchPassword/emailCertify", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object> searchPasswordCheckEmail(@RequestParam int inputCertifiedCode,
                                                        @RequestParam String certifiedKey) {
        Map<String, Object> response = new HashMap<>();
        boolean isPass = searchUserInfoService.certifyEmail(inputCertifiedCode, certifiedKey); // Member 부분에서

        response.put("result", isPass ? "success" : "fail");
        return response;
    }

    @PostMapping("/searchPassword")
    public String searchId(@RequestParam String userId,
                           @RequestParam String email,
                           @RequestParam int emailAuthenticationNumber,
                           @RequestParam int hiddenCertifiedKey,
                           RedirectAttributes redirectAttributes) throws MessagingException, UnsupportedEncodingException {
        String newUserPassword = searchUserInfoService.insertUserPassword(userId, email, emailAuthenticationNumber, hiddenCertifiedKey);

        if (newUserPassword != null) {
            redirectAttributes.addFlashAttribute("message", "임시비밀번호를 메일로 보냈습니다. 반드시 비밀번호를 변경해주세요.");
            return "redirect:/auth/login";
        } else if (newUserPassword.equals("sendMiss")) {
            redirectAttributes.addFlashAttribute("message", "임시비밀번호 전송을 실패했습니다. 메인화면으로 돌아갑니다.");
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("message", "알 수 없는 오류로 비밀번호 찾기에 실패했습니다. 메인화면으로 돌아갑니다.");
            return "redirect:/";
        }
    }


    @GetMapping("fail")
    public String loginFail(@RequestParam String message, Model model,
                            HttpServletRequest req,
                            HttpServletResponse res) {
        if (message.equals("withdrawCancel")) {
            model.addAttribute("message",
                    "탈퇴 유예 기간 중에 접속했습니다.\n탈퇴신청을 해제합니다.");
            return "/client/content/main/main";
        }
        SessionLogout.invalidSession(req, res);
        model.addAttribute("message", message);
        return "/client/content/auth/fail";
    }
}

