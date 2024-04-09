package com.hedgehog.client.board.model.service;

import com.hedgehog.client.board.model.dao.BoardDetailMapper;
import com.hedgehog.client.board.model.dto.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class BoardDetailService {
    private final BoardDetailMapper mapper;

    public ReviewDTO getReviewDetail(int postCode) {
        ReviewDTO reviewDTO = mapper.getReviewDetail(postCode);
        return reviewDTO;
    }

    public QuestionDTO getQuestionDetail(int postCode) {
        QuestionDTO questionDTO = mapper.getQuestionDetail(postCode);
        return questionDTO;
    }

    public NoticeDTO getNoticeDetail(int postCode) {
        NoticeDTO noticeDTO = mapper.getNoticeDetail(postCode);
        return noticeDTO;
    }

    public FaqDTO getFaqDetail(int postCode) {
        FaqDTO faqDTO = mapper.getFaqDetail(postCode);
        return faqDTO;
    }

    @Transactional
    public void addViews(int postCode) {
        int views = mapper.getViews(postCode);
        mapper.setViews(postCode, views + 1);
    }

    public int getReviewUserCode(String postCode) {
        int reviewUserCode = mapper.getReviewUserCode(postCode);
        return reviewUserCode;
    }

    public int getInquiryUserCode(String postCode) {
        int inquiryUserCode = mapper.getInquiryUserCode(postCode);
        return inquiryUserCode;
    }

    @Transactional
    public void deleteReview(String postCode, int userCode) {
        mapper.updateReviewState(postCode, userCode);
        ReviewDeleteDTO reviewDeleteDTO = mapper.getReviewPostGrade(postCode, userCode);
        int reviewGrade = reviewDeleteDTO.getGrade();
        int productCode = reviewDeleteDTO.getProductCode();
        mapper.updateReviewPostImage(postCode);
        ProductReviewDTO productReviewDTO = mapper.getReviewInfo(productCode);
        int reviews = productReviewDTO.getReviews();
        double grade = productReviewDTO.getGrade();
        ProductReviewDTO newProductReviewDTO = new ProductReviewDTO(reviews - 1, ((reviews - 1) != 0) ? (grade * reviews - reviewGrade) / (reviews - 1) : 0);
        mapper.updateProductReviewCount(newProductReviewDTO, productCode);
    }

    @Transactional
    public void deleteInquiry(String postCode, int userCode) {
        mapper.updateInquiryState(postCode, userCode);
        mapper.updateInquiryPostImage(postCode);
    }
}
