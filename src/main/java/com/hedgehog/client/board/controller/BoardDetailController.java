package com.hedgehog.client.board.controller;

import com.hedgehog.client.auth.model.dto.LoginDetails;
import com.hedgehog.client.board.model.dto.FaqDTO;
import com.hedgehog.client.board.model.dto.NoticeDTO;
import com.hedgehog.client.board.model.dto.QuestionDTO;
import com.hedgehog.client.board.model.dto.ReviewDTO;
import com.hedgehog.client.board.model.service.BoardDetailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/board/*")
@AllArgsConstructor
public class BoardDetailController {
    private final BoardDetailService boardDetailService;

    @GetMapping("detail")
    public ModelAndView getBoardDetail(@RequestParam int postType,
                                       @RequestParam int postCode,
                                       @RequestParam(required = false) String searchCondition,
                                       @RequestParam(required = false) String searchValue,
                                       @RequestParam(defaultValue = "gradeDESC", required = false) String orderBy,
                                       @RequestParam(value = "currentPage", defaultValue = "1") int pageNo,
                                       ModelAndView mv) {
        mv.addObject("postType", postType);
        if (postType == 1) {
            ReviewDTO reviewDTO = boardDetailService.getReviewDetail(postCode);
            mv.addObject("board", reviewDTO);
        } else if (postType == 2) {
            QuestionDTO questionDTO = boardDetailService.getQuestionDetail(postCode);
            mv.addObject("board", questionDTO);
        } else if (postType == 3) {
            boardDetailService.addViews(postCode);
            NoticeDTO noticeDTO = boardDetailService.getNoticeDetail(postCode);
            mv.addObject("board", noticeDTO);
        } else if (postType == 4) {
            boardDetailService.addViews(postCode);
            FaqDTO faqDTO = boardDetailService.getFaqDetail(postCode);
            mv.addObject("board", faqDTO);
        }

        mv.setViewName("client/content/board/boardDetail");
        return mv;
    }

    @PostMapping(value = "/detail/delete", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object> delete(@AuthenticationPrincipal LoginDetails loginDetails,
                                      @RequestParam String postType,
                                      @RequestParam String postCode) {
        Map<String, Object> response = new HashMap<>();
        if (loginDetails == null) {
            response.put("result", "guest");
            return response;
        }

        int userCode = loginDetails.getLoginUserDTO().getUserCode();

        if (!postType.equals("1") && !postType.equals("2")) {
            response.put("result", "fail");
            return response;
        }
        if (postType.equals("1")) {
            int postUserCode = boardDetailService.getReviewUserCode(postCode);
            if (userCode != postUserCode) {
                response.put("result", "fail");
                return response;
            }
            boardDetailService.deleteReview(postCode, userCode);
        } else {
            int postUserCode = boardDetailService.getInquiryUserCode(postCode);
            if (userCode != postUserCode) {
                response.put("result", "fail");
                return response;
            }
            boardDetailService.deleteInquiry(postCode, userCode);
        }

        response.put("result", "success");
        return response;
    }
}