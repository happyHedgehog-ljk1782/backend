package com.hedgehog.client.board.model.service;

import com.hedgehog.client.board.model.dao.BoardWriteMapper;
import com.hedgehog.client.board.model.dto.ProductReviewDTO;
import com.hedgehog.client.board.model.dto.UploadedImageDTO;
import com.hedgehog.client.orderDetails.model.dto.OrderDetailsDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class BoardWriteService {
    private final BoardWriteMapper mapper;

    @Transactional
    public boolean questionRegist(int userCode, String option, String inputTitle, String newEditordata, List<UploadedImageDTO> uploadedImageList) {
        int result = mapper.insertTblInquiry(userCode, option, inputTitle, newEditordata);
        if (result != 1) {
            return false;
        }
        Integer inquiryCode = mapper.getLastInsertCodeInquiry();
        if (inquiryCode == null) {
            return false;
        }

        if (uploadedImageList != null) {
            int result2 = mapper.insertPostImageInquiry(inquiryCode, uploadedImageList);

            if (result2 != uploadedImageList.size()) {
                return false;
            }
        }

        return true;
    }

    public String findMyIdByOrderDetailsCode(int orderDetailsCode) {
        String result = mapper.findMyIdByOrderDetailsCode(orderDetailsCode);

        return result;
    }

    public OrderDetailsDTO selectOrderDetail(int orderDetailsCode) {
        OrderDetailsDTO result = mapper.selectOrderDetail(orderDetailsCode);
        return result;
    }

    @Transactional
    public boolean reviewRegist(int userCode, String editordata, OrderDetailsDTO orderDetailsDTO, String stars, List<UploadedImageDTO> uploadedImageList) {
        int result = mapper.insertTblReview(userCode, editordata, orderDetailsDTO, stars);
        if (result != 1) {
            return false;
        }
        Integer reviewCode = mapper.getLastInsertCodeReview();
        if (reviewCode == null) {
            return false;
        }
        if (uploadedImageList != null) {
            int result2 = mapper.insertPostImageReview(reviewCode, uploadedImageList);
            if (result2 != uploadedImageList.size()) {
                return false;
            }
        }
        int result3 = mapper.updateReviewPoint(orderDetailsDTO.getOrderDetailsCode());
        if (result3 != 1) {
            return false;
        }
        Integer point = mapper.selectMemberPoint(userCode);
        if (point == null) {
            return false;
        }
        point += 1000;
        mapper.updateMemberPoint(userCode, point);

        ProductReviewDTO productReviewDTO = mapper.getReviewInfo(orderDetailsDTO.getProductCode());
        int reviews = productReviewDTO.getReviews();
        double grade = productReviewDTO.getGrade();
        ProductReviewDTO newProductReviewDTO = new ProductReviewDTO(reviews + 1, (grade * reviews + Integer.parseInt(stars)) / (reviews + 1));
        mapper.updateProductReviewCount(newProductReviewDTO, orderDetailsDTO.getProductCode());

        return true;
    }
}
