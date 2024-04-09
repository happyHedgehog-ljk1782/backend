package com.hedgehog.client.board.controller;

import com.hedgehog.client.auth.model.dto.LoginDetails;
import com.hedgehog.client.board.model.dto.*;
import com.hedgehog.client.board.model.service.BoardDetailService;
import com.hedgehog.client.board.model.service.BoardService;
import com.hedgehog.common.paging.SelectCriteria;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.hedgehog.common.paging.Pagenation;

@Controller
@RequestMapping("/board/*")
@AllArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/reviewList")
    public ModelAndView reviewList(@RequestParam(required = false) String searchCondition,
                                   @RequestParam(required = false) String searchValue,
                                   @RequestParam(required = false) String orderBy,
                                   @RequestParam(value = "currentPage", defaultValue = "1") int pageNo,
                                   ModelAndView mv) {
        Map<String, String> searchMap = new HashMap<>();
        searchMap.put("searchCondition", searchCondition);
        searchMap.put("searchValue", searchValue);
        searchMap.put("orderBy", orderBy);
        int totalCount = boardService.selectTotalCountReviewList(searchMap);
        int limit = 3;
        int buttonAmount = 5;
        SelectCriteria selectCriteria = null;
        if (searchCondition != null && !"".equals(searchCondition)) {
            selectCriteria = Pagenation.getSelectCriteria(pageNo, totalCount, limit, buttonAmount,
                    searchCondition, searchValue, orderBy);
        } else {
            selectCriteria = Pagenation.getSelectCriteria(pageNo, totalCount, limit, buttonAmount, orderBy);
        }
        List<ReviewDTO> reviewList = boardService.selectReviewList(selectCriteria);
        List<Integer> reviewCodes = reviewList.stream().map(ReviewDTO::getReviewCode).collect(Collectors.toList());
        List<PostImageDTO> imageList = boardService.getReviewImage(reviewCodes);
        mv.addObject("reviewList", reviewList);
        mv.addObject("imageList", imageList);
        mv.addObject("selectCriteria", selectCriteria);
        mv.setViewName("/client/content/board/reviewList");
        return mv;
    }

    @GetMapping("/questionList")
    public ModelAndView questionList(@RequestParam(required = false) String searchCondition,
                                     @RequestParam(required = false) String searchValue,
                                     @RequestParam(value = "currentPage", defaultValue = "1") int pageNo,
                                     ModelAndView mv) {
        Map<String, String> searchMap = new HashMap<>();
        searchMap.put("searchCondition", searchCondition);
        searchMap.put("searchValue", searchValue);

        int totalCount = boardService.selectTotalCountQuestionList(searchMap);

        int limit = 5;
        int buttonAmount = 5;
        SelectCriteria selectCriteria = null;

        if (searchCondition != null && !"".equals(searchCondition)) {
            selectCriteria = Pagenation.getSelectCriteria(pageNo, totalCount, limit, buttonAmount, searchCondition, searchValue);
        } else {
            selectCriteria = Pagenation.getSelectCriteria(pageNo, totalCount, limit, buttonAmount);
        }

        List<QuestionDTO> questionList = boardService.selectQuestionList(selectCriteria);

        mv.addObject("questionList", questionList);
        mv.addObject("selectCriteria", selectCriteria);
        mv.setViewName("/client/content/board/questionList");

        return mv;
    }

    @GetMapping("/noticeList")
    public ModelAndView noticeList(@RequestParam(defaultValue = "writeDateDESC") String orderBy,
                                   @RequestParam(value = "currentPage", defaultValue = "1") int pageNo,
                                   ModelAndView mv) {
        Map<String, String> searchMap = new HashMap<>();
        searchMap.put("orderBy", orderBy);

        int totalCount = boardService.selectTotalCountNoticeList(searchMap);

        int limit = 5;
        int buttonAmount = 5;
        SelectCriteria selectCriteria = null;
        if (orderBy != null) {
            selectCriteria = Pagenation.getSelectCriteria(pageNo, totalCount, limit, buttonAmount, orderBy);
        } else {
            selectCriteria = Pagenation.getSelectCriteria(pageNo, totalCount, limit, buttonAmount);
        }

        List<NoticeDTO> noticeList = boardService.selectNoticeList(selectCriteria);

        mv.addObject("noticeList", noticeList);
        mv.addObject("selectCriteria", selectCriteria);
        mv.setViewName("/client/content/board/noticeList");

        return mv;
    }

    @GetMapping("/faqList")
    public ModelAndView faqList(@RequestParam(defaultValue = "writeDateDESC") String orderBy,
                                @RequestParam(value = "currentPage", defaultValue = "1") int pageNo,
                                ModelAndView mv) {
        Map<String, String> searchMap = new HashMap<>();
        searchMap.put("orderBy", orderBy);

        int totalCount = boardService.selectTotalCountFaqList(searchMap);

        int limit = 5;
        int buttonAmount = 5;
        SelectCriteria selectCriteria = null;

        if (orderBy != null) {
            selectCriteria = Pagenation.getSelectCriteria(pageNo, totalCount, limit, buttonAmount, orderBy);
        } else {
            selectCriteria = Pagenation.getSelectCriteria(pageNo, totalCount, limit, buttonAmount);
        }
        List<FaqDTO> faqList = boardService.selectFaqList(selectCriteria);

        mv.addObject("faqList", faqList);
        mv.addObject("selectCriteria", selectCriteria);
        mv.setViewName("/client/content/board/faqList");
        return mv;
    }
}
