package com.hedgehog.config.handler;

import com.hedgehog.common.common.exception.UserWithdrawCancelException;
import com.hedgehog.common.common.exception.UserWithdrawException;
import com.hedgehog.common.login.dto.WithdrawStatus;
import com.hedgehog.common.login.service.WithdrawService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
@AllArgsConstructor
public class AuthSuccessHandler implements AuthenticationSuccessHandler {
    private final WithdrawService withdrawService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        /*로그인에 성공하면 일단 Withdraw를 조사한다.*/

        String userId = authentication.getName();
        int withdrawStatus = withdrawService.checkWithdrawStatus(userId);
        if (withdrawStatus == -1) {
            // 탈퇴한 계정이라고 throw
            throw new UserWithdrawException("이미 탈퇴한 계정");
        }

        String saveId = request.getParameter("saveId");
        if ("on".equals(saveId)) {
            Cookie userIdCookie = new Cookie("userId", userId);
            userIdCookie.setMaxAge(60 * 60 * 24 * 7);
            userIdCookie.setPath("/");
            response.addCookie(userIdCookie);
        } else {
            Cookie userIdCookie = new Cookie("userId", null);
            userIdCookie.setMaxAge(0);
            userIdCookie.setPath("/");
            response.addCookie(userIdCookie);
        }
        if (withdrawStatus == 1) {
            // 탈퇴신청중인 회원이었다고 throw. 예외는 아니지만 예외로 던진다.
            throw new UserWithdrawCancelException("탈퇴 유예 기간중에 접속 완료.");
        }

        response.sendRedirect("/");
    }
}
