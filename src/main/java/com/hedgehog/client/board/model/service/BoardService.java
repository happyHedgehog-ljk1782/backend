package com.hedgehog.client.board.model.service;

import com.hedgehog.client.board.model.dao.BoardMapper;
import com.hedgehog.client.board.model.dto.*;
import com.hedgehog.common.paging.SelectCriteria;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class BoardService {
    private final BoardMapper mapper;

    public int selectTotalCountQuestionList(Map<String, String> searchMap) {
        int result = mapper.selectTotalCountQuestionList(searchMap);

        return result;
    }

    public int selectTotalCountReviewList(Map<String, String> searchMap) {
        int result = mapper.selectTotalCountReviewList(searchMap);

        return result;
    }

    public List<QuestionDTO> selectQuestionList(SelectCriteria selectCriteria) {
        List<QuestionDTO> result = mapper.selectQuestionList(selectCriteria);

        return result;
    }

    public List<ReviewDTO> selectReviewList(SelectCriteria selectCriteria) {
        List<ReviewDTO> result = mapper.selectReviewList(selectCriteria);

        return result;
    }

    public int selectTotalCountNoticeList(Map<String, String> searchMap) {
        int result = mapper.selectTotalCountNoticeList(searchMap);

        return result;
    }

    public List<NoticeDTO> selectNoticeList(SelectCriteria selectCriteria) {
        List<NoticeDTO> result = mapper.selectNoticeList(selectCriteria);

        return result;
    }

    public int selectTotalCountFaqList(Map<String, String> searchMap) {
        int result = mapper.selectTotalCountFaqList(searchMap);

        return result;
    }

    public List<FaqDTO> selectFaqList(SelectCriteria selectCriteria) {
        List<FaqDTO> result = mapper.selectFaqList(selectCriteria);

        return result;
    }

    public List<PostImageDTO> getReviewImage(List<Integer> reviewCodes) {
        List<PostImageDTO> result = mapper.getReviewImage(reviewCodes);

        return result;
    }
}
